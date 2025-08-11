package kz.haru.implement.modules.movement;

import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.module.setting.settings.BooleanSetting;
import kz.haru.common.utils.math.TimerUtil;
import kz.haru.common.utils.player.movement.MoveUtil;
import kz.haru.common.utils.player.world.PlayerUtil;
import kz.haru.implement.events.connection.CloseScreenEvent;
import kz.haru.implement.events.connection.HPacketEvent;
import kz.haru.implement.events.player.updates.UpdateEvent;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.EditSignScreen;
import net.minecraft.client.gui.screen.inventory.AnvilScreen;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CClickWindowPacket;

import java.util.LinkedList;

@ModuleRegister(name = "Gui Move", category = Category.MOVEMENT, desc = "Позволяет двигаться в менюшках")
public class GuiMoveModule extends Module {
    public final BooleanSetting slowness = new BooleanSetting("Slowness").value(false);
    private final BooleanSetting sneak = new BooleanSetting("Sneak").value(false);

    public GuiMoveModule() {
        setup(slowness, sneak);
    }
    
    public static GuiMoveModule get() {
        return Module.get(GuiMoveModule.class);
    }

    public static boolean slownessIsEnabled() {
        return get().slowness.getValue();
    }

    private final LinkedList<IPacket<?>> packets = new LinkedList<>();
    private final TimerUtil timerUtil = new TimerUtil();
    public boolean slowed;

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!timerUtil.hasReached(100) && slowness.getValue()) {
            slowed = true;
            for (KeyBinding keyBinding : movementKeys()) {
                keyBinding.setPressed(false);
            }
            return;
        }

        slowed = false;

        if (PlayerUtil.isStoppedByModule(getName()) || mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof AnvilScreen || mc.currentScreen instanceof EditSignScreen) {
            return;
        }

        if (mc.currentScreen instanceof ChestScreen && slowness.getValue() && sneak.getValue()) {
            mc.gameSettings.keyBindSneak.setPressed(true);
        }

        updateMovementKeys(movementKeys());
    }

    @EventTarget
    public void onPacket(HPacketEvent event) {
        if (MoveUtil.isMoving() || mc.gameSettings.keyBindJump.pressed) {
            if (mc.currentScreen instanceof ContainerScreen<?>) {
                if (event.getPacket() instanceof CClickWindowPacket windowPacket) {
                    packets.add(windowPacket);
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventTarget
    public void onCloseScreen(CloseScreenEvent event) {
        if (event.getScreen() instanceof ContainerScreen<?> && slowness.getValue() && !packets.isEmpty()) {
            new Thread(() -> {
                slowed = true;
                timerUtil.reset();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                while (!packets.isEmpty()) {
                    var p = packets.removeLast();
                    mc.player.connection.sendPacketWithoutEvent(p);
                }
                slowed = false;
            }).start();

            event.setCancelled(true);
        }
    }

    public void updateMovementKeys(KeyBinding[] keyBindings) {
        for (KeyBinding key : keyBindings) {
            key.setPressed(InputMappings.isKeyDown(mc.mainWindow.getHandle(), key.getDefault().getKeyCode()));
        }
    }

    public KeyBinding[] movementKeys() {
        return new KeyBinding[]{
                mc.gameSettings.keyBindForward,
                mc.gameSettings.keyBindBack,
                mc.gameSettings.keyBindRight,
                mc.gameSettings.keyBindLeft,
                mc.gameSettings.keyBindJump,
                mc.gameSettings.keyBindSprint,
        };
    }
}
