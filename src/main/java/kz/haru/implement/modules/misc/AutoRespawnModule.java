package kz.haru.implement.modules.misc;

import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.event.EventTarget;
import kz.haru.implement.events.player.updates.UpdateEvent;
import net.minecraft.client.gui.screen.DeathScreen;

@ModuleRegister(name = "Auto Respawn", category = Category.MISC, desc = "Автоматически возвраждает после смерти")
public class AutoRespawnModule extends Module {
    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.currentScreen instanceof DeathScreen) {
            if (mc.player.deathTime > 2) {
                mc.player.respawnPlayer();
                mc.displayGuiScreen(null);
            }
        }
    }
}
