package kz.haru.implement.modules.misc;

import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.system.friends.FriendManager;
import kz.haru.implement.events.connection.HPacketEvent;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.text.ITextComponent;

@ModuleRegister(name = "Auto Tpa Accept", category = Category.MISC, desc = "Принимает запросы от на телепортацию от друзей")
public class AutoTpaAcceptModule extends Module {
    @EventTarget
    public void onPacket(HPacketEvent event) {
        if (!isEnabled() || !event.isReceive()) return;

        if (event.getPacket() instanceof SChatPacket packet) {
            ITextComponent message = packet.getChatComponent();
            String messageText = message.getString();

            if (messageText.contains("телепортироваться")) {

                String[] parts = messageText.split(" ");
                if (parts.length > 0) {
                    String playerName = extractPlayerName(parts[0]);


                    if (playerName != null && FriendManager.isFriend(playerName)) {

                        mc.player.sendChatMessage("/tpaaccept");
                    }
                }
            }
        }
    }

    private String extractPlayerName(String text) {
        if (text.startsWith("[") && text.endsWith("]")) {
            return text.substring(1, text.length() - 1);
        }
        return null;
    }
}