package kz.haru.implement.modules.combat;

import kz.haru.implement.events.connection.HPacketEvent;
import kz.haru.implement.modules.movement.GuiMoveModule;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.item.minecart.TNTMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.module.setting.settings.FloatSetting;
import kz.haru.api.module.setting.settings.MultiModeSetting;
import kz.haru.common.utils.math.TimerUtil;
import kz.haru.common.utils.player.world.InventoryUtil;
import kz.haru.implement.events.minecraft.MTickEvent;
import net.minecraft.network.play.server.SEntityStatusPacket;
import net.minecraft.util.Hand;

@ModuleRegister(name = "Auto Totem", category = Category.COMBAT, desc = "Берет тотем в левую руку при опастности")
public class AutoTotemModule extends Module {
    private final FloatSetting health = new FloatSetting("Health").value(5f).range(0f, 20f).step(0.5f);
    private final MultiModeSetting options = new MultiModeSetting("Options").select("Swap back").addValues("Swap back", "No ball switch", "Save enchanted");
    private final MultiModeSetting checks = new MultiModeSetting("Checks").select("Absorption").addValues("Absorption", "Crystals", "Falling", "Elytra");
    private final FloatSetting healthWithElytra = new FloatSetting("Health with elytra").value(10f).range(0f, 20f).step(0.5f).setVisible(() -> checks.is("Elytra"));

    private final TimerUtil timerUtil = new TimerUtil();
    private int oldItem = -1;
    private boolean totemIsUsed = false;
    private int nonEnchantedTotems = 0;
    private boolean isTotemPlaced = false;

    public AutoTotemModule() {
        setup(health, options, checks, healthWithElytra);
    }

    @EventTarget
    public void onTick(MTickEvent event) {
        updateTotemCount();
        handleTotemSwapping();
    }

    @EventTarget
    public void onPacket(HPacketEvent event) {
        handleTotemUsePacket(event);
    }

    @Override
    public void onDisable() {
        resetState();
        super.onDisable();
    }

    private void updateTotemCount() {
        nonEnchantedTotems = InventoryUtil.countNonEnchantedTotems();
    }

    private void handleTotemSwapping() {
        if (!timerUtil.hasReached(400)) return;

        if (shouldPlaceTotem()) {
            placeTotem();
            return;
        }

        if (shouldReturnItem() && !canSwap()) {
            returnOriginalItem();
        }
    }

    private boolean shouldPlaceTotem() {
        int slot = InventoryUtil.findBestTotemSlot(options.is("Save enchanted"));
        return canSwap() && slot != -1 && !hasTotemInHand();
    }

    private boolean shouldReturnItem() {
        return oldItem != -1 && options.is("Swap back");
    }

    private void placeTotem() {
        int slot = InventoryUtil.findBestTotemSlot(options.is("Save enchanted"));
        saveCurrentItem(slot);
        swapToOffhand(slot);
        isTotemPlaced = true;
        timerUtil.reset();
    }

    private void returnOriginalItem() {
        swapToOffhand(oldItem);
        isTotemPlaced = false;
        oldItem = -1;
        timerUtil.reset();
    }

    private void saveCurrentItem(int slot) {
        if (mc.player.getHeldItemOffhand().getItem() != Items.AIR && oldItem == -1) {
            oldItem = slot;
        }
    }

    private void handleTotemUsePacket(HPacketEvent event) {
        if (!event.isReceive()) return;

        if (event.getPacket() instanceof SEntityStatusPacket packet) {
            if (packet.getOpCode() == 35 && packet.getEntity(mc.world) == mc.player) {
                totemIsUsed = true;
                isTotemPlaced = false;
            }
        }
    }

    private boolean hasTotemInHand() {
        return (mc.player.getHeldItem(Hand.MAIN_HAND).getItem() == Items.TOTEM_OF_UNDYING &&
                isNotSaveEnchanted(mc.player.getHeldItem(Hand.MAIN_HAND))) ||
                (mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING &&
                        isNotSaveEnchanted(mc.player.getHeldItemOffhand()));
    }

    private boolean isNotSaveEnchanted(ItemStack stack) {
        return !options.is("Save enchanted") || !stack.isEnchanted() || nonEnchantedTotems <= 0;
    }

    private boolean canSwap() {
        float healthWithAbsorption = calculateEffectiveHealth();
        float finalHealth = mc.player.inventory.armorItemInSlot(2).getItem() == Items.ELYTRA && checks.is("Elytra")
                ? healthWithElytra.getValue()
                : health.getValue();

        if (isOffhandProtectedItem()) return false;
        if (isInDanger()) return true;
        return healthWithAbsorption <= finalHealth;
    }

    private float calculateEffectiveHealth() {
        float absorption = checks.is("Absorption") ? mc.player.getAbsorptionAmount() : 0f;
        return mc.player.getHealth() + absorption;
    }

    private boolean isOffhandProtectedItem() {
        if (shouldIgnoreProtection()) return false;
        return options.is("No ball switch") &&
                mc.player.getHeldItemOffhand().getItem() == Items.PLAYER_HEAD;
    }

    private boolean shouldIgnoreProtection() {
        return checks.is("Falling") && mc.player.fallDistance > 5f;
    }

    private boolean isInDanger() {
        return checkCrystals() || checkFalling();
    }

    private boolean checkCrystals() {
        if (!checks.is("Crystals")) return false;
        for (Entity entity : mc.world.getAllEntities()) {
            if (isDangerousEntity(entity)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDangerousEntity(Entity entity) {
        return (entity instanceof EnderCrystalEntity || entity instanceof TNTMinecartEntity || entity instanceof TNTEntity) &&
                mc.player.getDistance(entity) <= 6f;
    }

    private boolean checkFalling() {
        if (!checks.is("Falling")) return false;
        if (mc.player.isInWater()) return false;
        if (mc.player.isElytraFlying()) return false;
        return mc.player.fallDistance > 10f;
    }

    private void swapToOffhand(int slot) {
        if (GuiMoveModule.slownessIsEnabled()) {
            InventoryUtil.applySlowness(60, () -> {
                InventoryUtil.swapToOffhand(slot);
            });
        } else {
            InventoryUtil.swapToOffhand(slot);
        }
    }

    private void resetState() {
        oldItem = -1;
        totemIsUsed = false;
        isTotemPlaced = false;
    }
}