package kz.haru.implement.modules.render;

import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.implement.events.render.Render2DEvent;
import kz.haru.implement.events.player.updates.PostRotationMovementInputEvent;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;

@ModuleRegister(name = "FullBright", category = Category.RENDER, desc = "Глаз-алмаз")
public class FullBrightModule extends Module {
    
    private long lastCheckTime = 0;
    private static final long CHECK_INTERVAL = 1000;
    private boolean needsReapply = false;

    @Override
    public void onEnable() {
        super.onEnable();
        applyNightVision();
        lastCheckTime = System.currentTimeMillis();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        removeNightVision();
    }
    
    @EventTarget
    public void onRender(Render2DEvent event) {
        if (isEnabled() && mc.player != null && event.isFirstLayer()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastCheckTime > CHECK_INTERVAL || needsReapply) {
                lastCheckTime = currentTime;
                needsReapply = false;
                if (!mc.player.isPotionActive(Effects.NIGHT_VISION) || 
                    (mc.player.getActivePotionEffect(Effects.NIGHT_VISION) != null && 
                     mc.player.getActivePotionEffect(Effects.NIGHT_VISION).getDuration() < 300)) {
                    applyNightVision();
                }
            }
        }
    }
    
    @EventTarget
    public void onPlayerUpdate(PostRotationMovementInputEvent event) {
        if (isEnabled() && mc.player != null && !mc.player.isPotionActive(Effects.NIGHT_VISION)) {
            needsReapply = true;
        }
    }
    
    private void applyNightVision() {
        if (mc.player != null) {
            mc.player.addPotionEffect(new EffectInstance(Effects.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
        }
    }
    
    private void removeNightVision() {
        if (mc.player != null) {
            mc.player.removePotionEffect(Effects.NIGHT_VISION);
        }
    }
}
