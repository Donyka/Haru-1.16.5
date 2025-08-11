package kz.haru.implement.modules.player;

import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.module.setting.settings.FloatSetting;
import kz.haru.api.module.setting.settings.ModeSetting;
import kz.haru.common.utils.math.TimerUtil;
import kz.haru.implement.events.player.updates.UpdateEvent;

@ModuleRegister(name = "Tape Mouse", category = Category.PLAYER, desc = "Клац-клац")
public class TapeMouseModule extends Module {
    private final ModeSetting mouseClick = new ModeSetting("Mouse click").addValues("Left", "Right").value("Left");
    private final FloatSetting clickDelay = new FloatSetting("Click delay").value(1000f).range(100f, 2000f).step(100f);

    private final TimerUtil timerUtil = new TimerUtil();

    public TapeMouseModule() {
        setup(mouseClick, clickDelay);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null) return;

        long delay = clickDelay.getValue().longValue();
        if (timerUtil.hasReached(delay)) {
            String clickType = mouseClick.getValue();

            if (clickType.equals("Left")) {
                mc.clickMouse();
            } else if (clickType.equals("Right")) {
                mc.rightClickMouse();
            }

            timerUtil.reset();
        }
    }
}