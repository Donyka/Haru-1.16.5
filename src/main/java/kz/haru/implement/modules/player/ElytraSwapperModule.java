package kz.haru.implement.modules.player;

import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.module.setting.settings.BindSetting;
import kz.haru.api.module.setting.settings.BooleanSetting;
import kz.haru.api.module.setting.settings.ModeSetting;
import kz.haru.common.utils.player.world.InventoryUtil;
import kz.haru.implement.events.input.ButtonInputEvent;
import kz.haru.implement.events.player.updates.UpdateEvent;
import kz.haru.implement.events.render.Render2DEvent;
import kz.haru.implement.modules.movement.GuiMoveModule;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

@ModuleRegister(name = "Elytra Swapper", category = Category.PLAYER, desc = "Очень удобное использование элитр и фейерверков")
public class ElytraSwapperModule extends Module {
    private final BindSetting swapKey = new BindSetting("Swap key").value(-1);
    private final BindSetting launchKey = new BindSetting("Launch key").value(-1);
    private final ModeSetting swapMode = new ModeSetting("Swap mode").value("Pick").addValues("Packet", "Pick");
    private final BooleanSetting autoJump = new BooleanSetting("Auto Jump").value(false);
    private final BooleanSetting alwaysJumping = new BooleanSetting("Always Jumping").value(false).setVisible(() -> autoJump.getValue());
    private final BooleanSetting autoFirework = new BooleanSetting("Auto Firework").value(false).setVisible(() -> autoJump.getValue());

    public ElytraSwapperModule() {
        setup(swapKey, launchKey, swapMode, autoJump, alwaysJumping, autoFirework);
    }

    private ItemStack currentStack = ItemStack.EMPTY;
    private ItemStack lastItemStack;
    private final int oldSlot = -1;
    private boolean waitingForJump = false;
    private long jumpDelay = 0;
    private long autoFlyTime = 0;
    private boolean checkForFall = false;
    private double lastY = 0.0;

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        currentStack = mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST);
        
        // Handle auto jump when elytra is equipped
        if (waitingForJump && System.currentTimeMillis() > jumpDelay) {
            if (currentStack.getItem() == Items.ELYTRA) {
                // Jump if on ground
                if (mc.player.isOnGround()) {
                    mc.player.jump();
                    // Set a timer for auto fly activation
                    autoFlyTime = System.currentTimeMillis() + 70; // Give some time for the jump to happen
                    checkForFall = true;
                    lastY = mc.player.getPosY();
                }
                waitingForJump = false;
            }
        }
        
        // Handle always jumping mode
        if (autoJump.getValue() && alwaysJumping.getValue() && currentStack.getItem() == Items.ELYTRA) {
            if (mc.player.isOnGround() && hasThreeBlocksOfAirAbove()) {
                mc.player.jump();
                // Set a timer for auto fly activation
                autoFlyTime = System.currentTimeMillis() + 70;
                checkForFall = true;
                lastY = mc.player.getPosY();
            }
        }
        
        // Handle auto fly activation
        if (autoFlyTime > 0 && System.currentTimeMillis() > autoFlyTime) {
            if (currentStack.getItem() == Items.ELYTRA && !mc.player.isOnGround() && !mc.player.isElytraFlying()) {
                if (ElytraItem.isUsable(currentStack)) {
                    // Start elytra flying
                    mc.player.startFallFlying();
                    mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
                    
                    // Use firework if auto firework is enabled
                    if (autoFirework.getValue()) {
                        firework();
                    }
                }
            }
            autoFlyTime = 0;
        }
        
        // Check for falling state to enter glide mode
        if (checkForFall && currentStack.getItem() == Items.ELYTRA && !mc.player.isOnGround() && !mc.player.isElytraFlying()) {
            double currentY = mc.player.getPosY();
            // If player is falling (Y position is decreasing)
            if (currentY < lastY) {
                if (ElytraItem.isUsable(currentStack)) {
                    // Start elytra flying
                    mc.player.startFallFlying();
                    mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
                    
                    // Use firework if auto firework is enabled
                    if (autoFirework.getValue()) {
                        firework();
                    }
                    
                    checkForFall = false; // Reset flag after activation
                }
            }
            lastY = currentY; // Update last Y position
        }
        
        // Reset the checkForFall flag if we're on ground or already flying
        if (mc.player.isOnGround() || mc.player.isElytraFlying()) {
            checkForFall = false;
        }
    }

    @EventTarget
    public void onRender(Render2DEvent event) {
        if (lastItemStack != null && event.isFirstLayer()) {
            new Thread(() -> {
                try {
                    Item toItem = mc.player.inventory.getStackInSlot(7).getItem();
                    int toSlot = toItem == Items.ELYTRA || toItem == Items.NETHERITE_CHESTPLATE || toItem == Items.DIAMOND_CHESTPLATE ? 6 : 7;
                    mc.player.inventory.setInventorySlotContents(toSlot, lastItemStack);
                    Thread.sleep(150);
                    lastItemStack = null;
                } catch (InterruptedException ignored) {
                }
            }).start();
        }
    }

    @EventTarget
    public void onButton(ButtonInputEvent event) {
        if (event.getAction() != 1 || mc.currentScreen != null) return;

        if (swapKey.getValue() == event.getButton()) {
            if (!GuiMoveModule.slownessIsEnabled()) {
                swapElytra(currentStack);
                if (autoJump.getValue()) {
                    waitingForJump = true;
                    jumpDelay = System.currentTimeMillis() + 50; // 50ms delay as suggested
                }
            } else {
                InventoryUtil.applySlowness(150, () -> {
                    swapElytra(currentStack);
                    if (autoJump.getValue()) {
                        waitingForJump = true;
                        jumpDelay = System.currentTimeMillis() + 150; // Longer delay if slowness is enabled
                    }
                });
            }
        }

        if (launchKey.getValue() == event.getButton() && mc.player.isElytraFlying()) {
            firework();
        }
    }

    private void firework() {
        if (mc.player.getHeldItemOffhand().getItem() == Items.FIREWORK_ROCKET) {
            InventoryUtil.useItem(Hand.OFF_HAND);
            return;
        }

        if (mc.player.getHeldItemMainhand().getItem() == Items.FIREWORK_ROCKET) {
            InventoryUtil.useItem(Hand.MAIN_HAND);
            return;
        }

        int fwHb = InventoryUtil.findItem(Items.FIREWORK_ROCKET, true);
        int fwInv = InventoryUtil.findItem(Items.FIREWORK_ROCKET, false);

        if (swapMode.is("Packet")) {
            if (fwHb != -1) {
                InventoryUtil.swapToSlot(fwHb);
                InventoryUtil.useItem(Hand.MAIN_HAND);
                InventoryUtil.swapToSlot(mc.player.inventory.currentItem);
            } else if (fwInv != -1) {
                finalInvFirework(swapMode.getValue(), fwInv);
            }
        } else {
            if (fwHb != -1) {
                mc.playerController.pickItem(fwHb);
                InventoryUtil.useItem(Hand.MAIN_HAND);
                mc.playerController.pickItem(fwHb);
                mc.playerController.pickItem(mc.player.inventory.currentItem);
            }
            if (fwInv != -1 && fwHb == -1) {
                finalInvFirework(swapMode.getValue(), fwInv);
            }
        }
    }

    private void finalInvFirework(String mode, int fwInv) {
        if (GuiMoveModule.slownessIsEnabled()) {
            InventoryUtil.applySlowness(150, () -> {
                fromInvFirework(mode, fwInv);
            });
        } else {
            fromInvFirework(mode, fwInv);
        }
    }

    private void fromInvFirework(String mode, int fwInv) {
        switch (mode) {
            case "Packet" -> {
                Item toItem = mc.player.inventory.getStackInSlot(7).getItem();
                int toSlot = toItem == Items.ELYTRA || toItem == Items.NETHERITE_CHESTPLATE || toItem == Items.DIAMOND_CHESTPLATE ? 6 : 7;
                if (lastItemStack == null) lastItemStack = mc.player.inventory.getStackInSlot(toSlot);
                mc.playerController.windowClick(0, fwInv, toSlot, ClickType.SWAP, mc.player);
                mc.player.connection.sendPacket(new CCloseWindowPacket(0));
                InventoryUtil.swapToSlot(toSlot);
                InventoryUtil.useItem(Hand.MAIN_HAND);
                InventoryUtil.swapToSlot(mc.player.inventory.currentItem);
                mc.playerController.windowClick(0, fwInv, toSlot, ClickType.SWAP, mc.player);
                mc.player.connection.sendPacket(new CCloseWindowPacket(0));
            }

            case "Pick" -> {
                mc.playerController.pickItem(fwInv);
                InventoryUtil.useItem(Hand.MAIN_HAND);
                mc.playerController.pickItem(fwInv);
                mc.playerController.pickItem(mc.player.inventory.currentItem);
            }
        }
    }

    private void swapElytra(ItemStack stack) {
        if (stack.getItem() != Items.ELYTRA) {
            int elytraSlot = InventoryUtil.findItem(Items.ELYTRA);
            if (elytraSlot != -1) {
                if (elytraSlot >= 36 && elytraSlot <= 44) {
                    int slotHB = InventoryUtil.findItem(Items.ELYTRA, true);

                    mc.playerController.windowClick(mc.player.openContainer.windowId, 6, slotHB, ClickType.SWAP, mc.player);
                    mc.player.connection.sendPacket(new CCloseWindowPacket(mc.player.openContainer.windowId));

                } else {
                    mc.playerController.windowClick(mc.player.openContainer.windowId, elytraSlot, 8, ClickType.SWAP, mc.player);
                    mc.player.connection.sendPacket(new CCloseWindowPacket(mc.player.openContainer.windowId));

                    mc.playerController.windowClick(mc.player.openContainer.windowId, 6, 8, ClickType.SWAP, mc.player);
                    mc.playerController.windowClick(mc.player.openContainer.windowId, oldSlot, 8, ClickType.SWAP, mc.player);

                    mc.player.connection.sendPacket(new CCloseWindowPacket(mc.player.openContainer.windowId));
                }

                // Track player's vertical position to detect falling
                if (!mc.player.isOnGround()) {
                    checkForFall = true;
                    lastY = mc.player.getPosY();
                }

                sendMessage("Свапнула на элитру!");
            } else {
                sendMessage("Не найдена элитра!");
                return;
            }
            return;
        }

        int armorSlot = getChestplate();
        if (armorSlot != -1) {
            if (armorSlot >= 36 && armorSlot <= 44) {
                int slotHB = InventoryUtil.findItem(Items.DIAMOND_CHESTPLATE, true);
                if (InventoryUtil.findItem(Items.NETHERITE_CHESTPLATE, true) != -1) {
                    slotHB = InventoryUtil.findItem(Items.NETHERITE_CHESTPLATE, true);
                }
                mc.playerController.windowClick(0, 6, slotHB, ClickType.SWAP, mc.player);
                mc.player.connection.sendPacket(new CCloseWindowPacket());

            } else {
                mc.playerController.windowClick(0, armorSlot, 8, ClickType.SWAP, mc.player);
                mc.player.connection.sendPacket(new CCloseWindowPacket());

                mc.playerController.windowClick(0, 6, 8, ClickType.SWAP, mc.player);
                mc.playerController.windowClick(0, oldSlot, 8, ClickType.SWAP, mc.player);

                mc.player.connection.sendPacket(new CCloseWindowPacket());
            }

            sendMessage("Свапнула на нагрудник!");
        } else {
            sendMessage("Не найден нагрудник!");
        }
    }

    private int getChestplate() {
        Item[] items = {Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE};

        for (Item item : items) {
            for (int i = 0; i < 36; ++i) {
                Item stack = mc.player.inventory.getStackInSlot(i).getItem();
                if (stack == item) {
                    if (i < 9) {
                        i += 36;
                    }
                    return i;
                }
            }
        }
        return -1;
    }

    // Check if there are 3 blocks of air above the player
    private boolean hasThreeBlocksOfAirAbove() {
        BlockPos playerPos = new BlockPos(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ());
        
        for (int i = 1; i <= 3; i++) {
            BlockPos checkPos = playerPos.up(i);
            if (!mc.world.isAirBlock(checkPos)) {
                return false;
            }
        }
        
        return true;
    }
}
