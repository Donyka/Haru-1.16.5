package kz.haru.client.functions;

import kz.haru.api.event.EventTarget;
import kz.haru.api.module.setting.settings.BooleanSetting;
import kz.haru.client.Haru;
import kz.haru.common.interfaces.IMinecraft;
import kz.haru.implement.events.connection.HPacketEvent;
import kz.haru.implement.events.minecraft.MTickEvent;
import kz.haru.implement.screen.clickgui.ClickGUIScreen;
import kz.haru.common.utils.client.TpsUtil;
import kz.haru.implement.events.render.Render2DEvent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.play.server.SUpdateTimePacket;

import static kz.haru.common.utils.player.world.InventoryUtil.*;

@Getter
@Setter
public class UpdateFunctions implements IMinecraft {
    @Getter
    private static UpdateFunctions instance = new UpdateFunctions();

    private float scaleFactor = 1f;
    private static final float baseWidth = 800f;
    private static final float baseHeight = 600f;

    public void init() {
        Haru.getInstance().getEventManager().register(this);
    }

    @EventTarget
    public void onRender(Render2DEvent event) {
        if (!event.isFirstLayer()) return;
        updateScaleFactor();
        updateAnimations();
    }

    @EventTarget
    public void onTick(MTickEvent event) {
        updateSlowness();
    }

    @EventTarget
    public void onPacket(HPacketEvent event) {
        if (event.getPacket() instanceof SUpdateTimePacket) {
            TpsUtil.onTimeUpdate();
        }
    }

    private void updateSlowness() {
        long currentTime = System.currentTimeMillis();

        if (isSlowed) {
            if (pendingAction != null && currentTime >= actionDelayTimer) {
                pendingAction.run();
                pendingAction = null;
            }

            if (currentTime >= resetTimer) {
                resetKeys();
            }
        }
    }

    private void updateAnimations() {
        Haru.getInstance().getModuleManager().getModules().forEach(module -> {
            module.getAnimation().run(module.isEnabled() ? 1.0 : 0.0);

            module.getSettings().forEach(setting -> {
                if (setting instanceof BooleanSetting booleanSetting) {
                    booleanSetting.getAnimation().run(booleanSetting.getValue() ? 1.0 : 0.0);
                }
            });
        });

        if (!(mc.currentScreen instanceof ClickGUIScreen) && !ClickGUIScreen.isExit) {
            ClickGUIScreen.alphaOpenCloseAnim.setValue(0.0);
            ClickGUIScreen.openCloseAnim.setValue(0.0);
            ClickGUIScreen.isExit = true;
        }
    }

    private void updateScaleFactor() {
        instance.scaleFactor = Math.min(window.getWidth() / baseWidth, window.getHeight() / baseHeight) * (kz.haru.implement.modules.render.InterfaceModule.get().hudScale.getValue() / 120f);
    }
}