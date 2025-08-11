package kz.haru.implement.modules.movement;

import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.implement.events.player.updates.UpdateEvent;

@ModuleRegister(name = "No Jump Delay", category = Category.MOVEMENT, desc = "Выключает задержку на прыжки")
public class NoJumpDelayModule extends Module {
    @EventTarget
    public void onUpdate(UpdateEvent event) {
        mc.player.jumpTicks = 0;
    }
}
