package kz.haru.implement.modules.misc;

import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.implement.events.connection.HPacketEvent;
import net.minecraft.network.play.client.CResourcePackStatusPacket;
import net.minecraft.network.play.server.SSendResourcePackPacket;

@ModuleRegister(name = "SRP Spoofer", category = Category.MISC, desc = "Пропускает проверку на серверный ресурс-пак")
public class SRPSpooferModule extends Module {
    @EventTarget
    public void onPacket(HPacketEvent event) {
        if (event.isReceive()) {
            if (event.getPacket() instanceof SSendResourcePackPacket packet) {
                mc.player.connection.sendPacket(new CResourcePackStatusPacket(CResourcePackStatusPacket.Action.ACCEPTED));
                mc.player.connection.sendPacket(new CResourcePackStatusPacket(CResourcePackStatusPacket.Action.SUCCESSFULLY_LOADED));
                event.setCancelled(true);
            }
        }
    }
}
