package kz.haru.implement.modules.render;

import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.module.setting.settings.MultiModeSetting;
import kz.haru.implement.events.player.updates.UpdateEvent;
import kz.haru.implement.events.render.RemovalsEvent;
import net.minecraft.potion.Effects;

@ModuleRegister(name = "Removals", category = Category.RENDER, desc = "Убирает выбранные элементы")
public class RemovalsModule extends Module {
    private final MultiModeSetting elements = new MultiModeSetting("Remove elements")
            .select("Bad effects", "Hurt camera")
            .addValues("Bad effects", "Hurt camera", "Grass", "Particles", "Fire", "Totem");

    public RemovalsModule() {
        setup(elements);
    }

    @EventTarget
    public void onRemovals(RemovalsEvent event) {
        event.setCancelled(
            switch (event.getCancelRender()) {
                case GRASS -> elements.is("Grass");
                case PARTICLES -> elements.is("Particles");
                case HURT_CAMERA -> elements.is("Hurt camera");
                case FIRE -> elements.is("Fire");
                case TOTEM -> elements.is("Totem");
            }
        );
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (elements.is("Bad effects")) {
            if (mc.player.isPotionActive(Effects.NAUSEA)) {
                mc.player.removePotionEffect(Effects.NAUSEA);
            }

            if (mc.player.isPotionActive(Effects.BLINDNESS)) {
                mc.player.removePotionEffect(Effects.BLINDNESS);
            }
        }
    }
}
