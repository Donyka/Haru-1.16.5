package kz.haru.common.utils.player.world;

import kz.haru.common.interfaces.IMinecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class InventoryUtil implements IMinecraft {
    private static final List<KeyBinding> movementKeys = new ArrayList<>() {{
        add(mc.gameSettings.keyBindForward);
        add(mc.gameSettings.keyBindBack);
        add(mc.gameSettings.keyBindLeft);
        add(mc.gameSettings.keyBindJump);
        add(mc.gameSettings.keyBindRight);
        add(mc.gameSettings.keyBindSprint);
    }};

    public static long actionDelayTimer = 0L;
    public static long resetTimer = 0L;
    public static boolean isSlowed = false;
    public static Runnable pendingAction = null;

    public static void applySlowness(long totalDelay, Runnable action) {
        if (isSlowed) return;

        for (KeyBinding key : movementKeys) {
            key.setPressed(false);
        }

        isSlowed = true;
        pendingAction = action;

        actionDelayTimer = System.currentTimeMillis() + MathHelper.clamp(totalDelay - 100, 50, 100);
        resetTimer = System.currentTimeMillis() + totalDelay;
    }

    public static void resetKeys() {
        for (KeyBinding key : movementKeys) {
            key.setPressed(InputMappings.isKeyDown(mc.getMainWindow().getHandle(), key.getDefault().getKeyCode()));
        }
        isSlowed = false;
        pendingAction = null;
    }

    public static int findItem(Item item, boolean inHotbar) {
        int firstSlot = inHotbar ? 0 : 9;
        int lastSlot = inHotbar ? 9 : 36;
        int finalSlot = -1;
        for (int i = firstSlot; i < lastSlot; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                finalSlot = i;
            }
        }

        return finalSlot;
    }

    public static int findItem(Item input) {
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.inventory.getStackInSlot(i);
            if (s.getItem() == input) {
                slot = i;
                break;
            }
        }
        if (slot < 9 && slot != -1) {
            slot = slot + 36;
        }
        return slot;
    }

    public static int findAxeInInventory(boolean inHotbar) {
        int firstSlot = inHotbar ? 0 : 9;
        int lastSlot = inHotbar ? 9 : 36;

        for (int i = firstSlot; i < lastSlot; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() instanceof AxeItem) {
                return i;
            }
        }
        return -1;
    }

    public static int findBestSlotInHotBar() {
        int emptySlot = findEmptySlot();
        if (emptySlot != -1) {
            return emptySlot;
        } else {
            return findNonSwordSlot();
        }
    }

    public static int findEmptySlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i).isEmpty() && mc.player.inventory.currentItem != i) {
                return i;
            }
        }
        return -1;
    }

    private static int findNonSwordSlot() {
        for (int i = 0; i < 9; i++) {
            if (!(mc.player.inventory.getStackInSlot(i).getItem() instanceof SwordItem) && !(mc.player.inventory.getStackInSlot(i).getItem() instanceof ElytraItem) && mc.player.inventory.currentItem != i) {
                return i;
            }
        }
        return -1;
    }

    public static int countNonEnchantedTotems() {
        int count = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING && !stack.isEnchanted()) {
                count++;
            }
        }
        return count;
    }

    public static int findBestTotemSlot(boolean saveEnchanted) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() != Items.TOTEM_OF_UNDYING) continue;

            if (!saveEnchanted || !stack.isEnchanted()) {
                return i < 9 ? i + 36 : i;
            }
        }
        return -1;
    }

    /**
     *
     * ДАЛЬШЕ ИДУТ ПЕНИСЫ
     *
     */

    public static void swapToOffhand(int slot) {
        if (slot == -1) return;
        if (mc.player == null || mc.playerController == null) return;

        int windowId = mc.player.openContainer.windowId;
        mc.playerController.windowClick(windowId, slot, 40, ClickType.SWAP, mc.player);
        mc.player.connection.sendPacket(new CCloseWindowPacket(windowId));
    }

    public static void swapSlots(int from, int to) {
        int windowId = mc.player.openContainer.windowId;
        int swapSlot = (to >= 0 && to <= 8) ? to : (from >= 0 && from <= 8) ? from : -1;
        if (swapSlot == -1) return;
        mc.playerController.windowClick(windowId, (swapSlot == to) ? from : to, swapSlot, ClickType.SWAP, mc.player);
        mc.player.connection.sendPacket(new CCloseWindowPacket(windowId));
    }

    public static void useItem(Hand hand) {
        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(hand));
    }

    public static void swapToSlot(int slot) {
        mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
    }
}