package kz.haru.implement.modules.misc;

import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.module.setting.settings.MultiModeSetting;
import kz.haru.implement.events.player.mechanics.PushingEvent;

@ModuleRegister(name = "No Push", category = Category.MISC, desc = "Иду против фикизи")
public class NoPushModule extends Module {
    private final MultiModeSetting cancelPushing = new MultiModeSetting("Cancel pushing").select("Block").addValues("Block", "Water", "Entity");

    public NoPushModule() {
        setup(cancelPushing);
    }

    @EventTarget
    public void onPushing(PushingEvent event) {
        event.setCancelled(
            switch (event.getPushingType()) {
                case BLOCK -> cancelPushing.is("Block");
                case WATER -> cancelPushing.is("Water");
                case ENTITY -> cancelPushing.is("Entity");
            }
        );
    }
}
