package kz.haru.implement.modules.player;

import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.module.setting.settings.FloatSetting;
import kz.haru.api.module.setting.settings.ModeSetting;
import kz.haru.api.system.friends.FriendManager;
import kz.haru.implement.events.player.updates.UpdateEvent;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

@ModuleRegister(name = "Auto Leave", category = Category.PLAYER, desc = "Жизнь интроверта :D")
public class AutoLeaveModule extends Module {
    private final ModeSetting action = new ModeSetting("Action").addValues("/hub", "/spawn", "/home", "kick").value("/home");
    private final FloatSetting distance = new FloatSetting("Distance").value(5f).range(1f, 100f).step(1f);

    public AutoLeaveModule() {
        setup(action, distance);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null) return;

        PlayerEntity player = mc.player;
        List<AbstractClientPlayerEntity> players = mc.world.getPlayers();

        for (PlayerEntity otherPlayer : players) {
            if (otherPlayer == player || isFriend(otherPlayer.getName().getString())) continue;

            double playerPosX = otherPlayer.getPosX();
            double playerPosY = otherPlayer.getPosY();
            double playerPosZ = otherPlayer.getPosZ();

            double localPlayerPosX = player.getPosX();
            double localPlayerPosY = player.getPosY();
            double localPlayerPosZ = player.getPosZ();

            double dx = playerPosX - localPlayerPosX;
            double dy = playerPosY - localPlayerPosY;
            double dz = playerPosZ - localPlayerPosZ;

            double distanceBetweenPlayers = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (distanceBetweenPlayers < distance.getValue()) {
                performAction(otherPlayer);
                toggle();
                break;
            }
        }
    }

    private void performAction(PlayerEntity player) {
        String actionValue = action.getValue();
        switch (actionValue) {
            case "/hub":
                mc.player.sendChatMessage("/hub");
                break;
            case "/spawn":
                mc.player.sendChatMessage("/spawn");
                break;
            case "/home":
                mc.player.sendChatMessage("/home");
                break;
            case "kick":
                mc.player.connection.getNetworkManager().closeChannel(new StringTextComponent("AutoLeave: Player too close! (" + player.getName().getString() + ")"));
                break;
        }
    }

    private boolean isFriend(String name) {
        return FriendManager.isFriend(name);
    }
}