package kz.haru.common.utils.player.attacking;

import kz.haru.common.interfaces.IMinecraft;
import kz.haru.common.utils.aiming.RaytracingUtil;
import kz.haru.common.utils.player.movement.MoveUtil;
import kz.haru.common.utils.player.world.InventoryUtil;
import kz.haru.implement.modules.movement.GuiMoveModule;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.math.EntityRayTraceResult;

public class AttackHandler implements IMinecraft {
    public final SprintManager sprintManager = new SprintManager();
    public final ClickScheduler clickScheduler = new ClickScheduler();

    public void handleAttack(AttackPerpetrator.AttackPerpetratorConfigurable configurable) {
        if (canAttack(configurable)) {
            if (mc.player.isBlocking() && configurable.unPressShield) {
                mc.playerController.onStoppedUsingItem(mc.player);
            }

            if (configurable.target instanceof PlayerEntity player && configurable.breakShield) {
                if (breakShieldPlayer(player)) return;
            }

            if (!mc.player.isOnLadder()) {
                sprintManager.preAttack();
            }

            mc.playerController.attackEntity(mc.player, configurable.target);
            mc.player.swingArm(Hand.MAIN_HAND);
            clickScheduler.recalculate(500L);

            sprintManager.postAttack();
        }
    }

    public boolean canAttack(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        if (clickScheduler.lastClickPassed() < 500) return false;
        if (isRaytraceFailed(config, getRaytraceEntity(config)) && !mc.player.isElytraFlying() || isCooldownNotComplete(config)) return false;

        if (!mc.gameSettings.keyBindJump.isPressed() && mc.player.isOnGround() && config.onlyCrits && config.onlySpaceCrits)
            return true;

        if (!mc.gameSettings.keyBindJump.isPressed() && MoveUtil.isAboveWater())
            return true;

        if (config.onlyCrits && !shouldCancelCrit()) {
            return isCriticalHit();
        }

        return true;
    }

    private boolean isRaytraceFailed(AttackPerpetrator.AttackPerpetratorConfigurable config, Entity targetEntity) {
        return config.raytraceEnabled && targetEntity != config.target;
    }

    private boolean isCooldownNotComplete(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        return !clickScheduler.isCooldownComplete();
    }

    private boolean shouldCancelCrit() {
        return mc.player.isPotionActive(Effects.BLINDNESS) ||
                mc.player.isPotionActive(Effects.LEVITATION) ||
                MoveUtil.isInWeb() ||
                mc.player.isInWater() ||
                mc.player.isInLava() ||
                mc.player.isOnLadder() ||
                mc.player.abilities.isFlying;
    }

    private boolean isCriticalHit() {
        return !mc.player.isOnGround() && mc.player.fallDistance > 0.1f;
    }

    private Entity getRaytraceEntity(AttackPerpetrator.AttackPerpetratorConfigurable configurable) {
        EntityRayTraceResult entityHitResult = RaytracingUtil.raytraceEntity(
                configurable.distance,
                configurable.rotation,
                configurable.ignoreWalls,
                entity -> entity.isAlive() && configurable.raytraceEnabled
        );

        if (entityHitResult != null) {
            return entityHitResult.getEntity();
        }

        return null;
    }

    private boolean breakShieldPlayer(PlayerEntity entity) {
        return breakShieldBase(entity, GuiMoveModule.slownessIsEnabled());
    }

    private boolean breakShieldBase(PlayerEntity entity, boolean slowness) {
        if (entity.isBlocking()) {
            int invSlot = InventoryUtil.findAxeInInventory(false);
            int hotBarSlot = InventoryUtil.findAxeInInventory(true);

            if (hotBarSlot == -1 && invSlot != -1) {
                if (!slowness) {
                    return shieldBreakAction("Inventory", hotBarSlot, invSlot, entity);
                } else {
                    InventoryUtil.applySlowness(150, () -> {
                        shieldBreakAction("Inventory", hotBarSlot, invSlot, entity);
                    });
                    return true;
                }
            }

            if (hotBarSlot != -1) {
                if (!slowness) {
                    shieldBreakAction("Hotbar", hotBarSlot, invSlot, entity);
                } else {
                    InventoryUtil.applySlowness(150, () -> {
                        shieldBreakAction("Hotbar", hotBarSlot, invSlot, entity);
                    });
                    return true;
                }
            }
        }

        return false;
    }

    private boolean shieldBreakAction(String action, int hotBarSlot, int invSlot, PlayerEntity entity) {
        int prevSlot = mc.player.inventory.currentItem;

        switch (action) {
            case "Hotbar" -> {
                InventoryUtil.swapToSlot(hotBarSlot);
                mc.playerController.attackEntity(mc.player, entity);
                mc.player.swingArm(Hand.MAIN_HAND);
                InventoryUtil.swapToSlot(prevSlot);
                return true;
            }

            case "Inventory" -> {
                int bestSlot = InventoryUtil.findBestSlotInHotBar();
                mc.playerController.windowClick(0, invSlot, bestSlot, ClickType.SWAP, mc.player);
                InventoryUtil.swapToSlot(bestSlot);

                mc.playerController.attackEntity(mc.player, entity);
                mc.player.swingArm(Hand.MAIN_HAND);

                mc.playerController.windowClick(0, invSlot, bestSlot, ClickType.SWAP, mc.player);
                mc.player.connection.sendPacket(new CCloseWindowPacket());
                InventoryUtil.swapToSlot(prevSlot);
                return true;
            }
        }

        return false;
    }
}
