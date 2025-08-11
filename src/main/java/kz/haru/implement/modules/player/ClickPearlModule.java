package kz.haru.implement.modules.player;

import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.module.setting.settings.BindSetting;
import kz.haru.api.module.setting.settings.ModeSetting;
import kz.haru.client.functions.KeyboardFunctions;
import kz.haru.common.utils.player.world.InventoryUtil;
import kz.haru.implement.events.minecraft.MTickEvent;
import kz.haru.implement.modules.movement.GuiMoveModule;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

@ModuleRegister(name = "Click Pearl", category = Category.PLAYER, desc = "Использует эндер-перл по бинду")
public class ClickPearlModule extends Module {
    private final BindSetting throwKey = new BindSetting("Throw key").value(-1);
    private final ModeSetting swapMode = new ModeSetting("Swap mode").value("Pick").addValues("Packet", "Pick");

    public ClickPearlModule() {
        setup(throwKey, swapMode);
    }

    private boolean isThrowed;

    @EventTarget
    public void onTick(MTickEvent event) {
        clickPearlLogic();
    }

    private void clickPearlLogic() {
        if (!KeyboardFunctions.isPressed(throwKey.getValue())) {
            isThrowed = false;
        }

        if (KeyboardFunctions.isPressed(throwKey.getValue()) && !isThrowed && mc.currentScreen == null) {
            int pHb = InventoryUtil.findItem(Items.ENDER_PEARL, true);
            int pInv = InventoryUtil.findItem(Items.ENDER_PEARL, false);

            if (mc.player.getHeldItemOffhand().getItem() == Items.ENDER_PEARL) {
                InventoryUtil.useItem(Hand.OFF_HAND);
                return;
            }

            if (mc.player.getHeldItemMainhand().getItem() == Items.ENDER_PEARL) {
                InventoryUtil.useItem(Hand.MAIN_HAND);
                return;
            }

            int previousSlot = mc.player.inventory.currentItem;

            if (pHb != -1) {
                if (swapMode.is("Packet")) {
                    InventoryUtil.swapToSlot(pHb);
                    InventoryUtil.useItem(Hand.MAIN_HAND);
                    InventoryUtil.swapToSlot(previousSlot);
                } else {
                    mc.playerController.pickItem(pHb);
                    InventoryUtil.useItem(Hand.MAIN_HAND);
                    mc.playerController.pickItem(mc.player.inventory.currentItem);
                }
            } else if (pInv != -1) {
                if (GuiMoveModule.slownessIsEnabled()) {
                    InventoryUtil.applySlowness(10, () -> {
                        fromInvPearl(pInv);
                    });
                } else {
                    fromInvPearl(pInv);
                }
            }

            isThrowed = true;
        }
    }

    private void fromInvPearl(int pearlSlot) {
        int toSlot = InventoryUtil.findEmptySlot();
        if (toSlot == -1) {
            toSlot = InventoryUtil.findBestSlotInHotBar();
        }

        int prevSlot = mc.player.inventory.currentItem;

        if (toSlot != -1) {
            if (swapMode.is("Packet")) {
                InventoryUtil.swapSlots(pearlSlot, toSlot);
                InventoryUtil.swapToSlot(toSlot);
                InventoryUtil.useItem(Hand.MAIN_HAND);
                InventoryUtil.swapToSlot(prevSlot);
                InventoryUtil.swapSlots(toSlot, pearlSlot);
            } else {
                mc.playerController.pickItem(pearlSlot);
                InventoryUtil.useItem(Hand.MAIN_HAND);
                mc.playerController.pickItem(pearlSlot);
                mc.playerController.pickItem(mc.player.inventory.currentItem);
            }
        }
    }
}
