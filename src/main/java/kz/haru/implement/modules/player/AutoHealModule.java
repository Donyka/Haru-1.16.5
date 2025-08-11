package kz.haru.implement.modules.player;

import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.module.setting.settings.FloatSetting;
import kz.haru.api.module.setting.settings.MultiModeSetting;
import kz.haru.common.utils.math.TimerUtil;
import kz.haru.implement.events.player.updates.UpdateEvent;

import java.util.Collections;
import java.util.List;

@ModuleRegister(name = "Auto Heal", category = Category.PLAYER, desc = "Already rich")
public class AutoHealModule extends Module {
    private final MultiModeSetting consider = new MultiModeSetting("Consider").addValues("Food", "Health").value(Collections.singletonList("Food"));
    private final FloatSetting healthValue = new FloatSetting("Health value").value(4f).range(0.1f, 20f).step(1f);
    private final FloatSetting foodValue = new FloatSetting("Food value").value(14f).range(0.1f, 20f).step(1f);

    private final TimerUtil timerUtil = new TimerUtil();

    public AutoHealModule() {
        setup(consider, healthValue, foodValue);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null) return;

        List<String> enabledOptions = consider.getValue();
        boolean shouldHeal = (mc.player.getHealth() + mc.player.getAbsorptionAmount() < healthValue.getValue() && enabledOptions.contains("Health")) ||
                (mc.player.getFoodStats().getFoodLevel() < foodValue.getValue() && enabledOptions.contains("Food"));

        performAction(shouldHeal);
    }

    private void performAction(boolean shouldHeal) {
        if (shouldHeal && timerUtil.hasReached(2000)) {
            mc.player.sendChatMessage("/heal");
            timerUtil.reset();
        }
    }
}