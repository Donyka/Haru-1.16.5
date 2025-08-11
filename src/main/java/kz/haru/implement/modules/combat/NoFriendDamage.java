package kz.haru.implement.modules.combat;

import com.google.common.eventbus.Subscribe;

import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.system.friends.FriendManager;
import kz.haru.implement.events.connection.HPacketEvent;

import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CUseEntityPacket;

@ModuleRegister(name = "NoFriendDamage", category = Category.COMBAT, desc = "Не дает бить по друзьям")
public class NoFriendDamage extends Module {

	@EventTarget
    public void onEvent(HPacketEvent event) {
        if (event.getPacket() instanceof CUseEntityPacket) {
            CUseEntityPacket cUseEntityPacket = (CUseEntityPacket) event.getPacket();
            Entity entity = cUseEntityPacket.getEntityFromWorld(mc.world);
            if (entity instanceof RemoteClientPlayerEntity &&
                    FriendManager.isFriend(entity.getName().getString()) &&
                    cUseEntityPacket.getAction() == CUseEntityPacket.Action.ATTACK) {
                event.setCancelled(true);
            }
        }
    }

}
