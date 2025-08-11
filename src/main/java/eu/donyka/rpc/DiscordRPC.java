package eu.donyka.rpc;


import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRichPresence;
import kz.haru.client.ClientInfo;
import kz.haru.common.interfaces.IMinecraft;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DiscordRPC implements IMinecraft {

    public static String state = "Version: " + ClientInfo.clientVersion;
    public static DiscordRichPresence discordRichPresence = new DiscordRichPresence();
    public static club.minnced.discord.rpc.DiscordRPC discordRPC = club.minnced.discord.rpc.DiscordRPC.INSTANCE;

    public static void startRPC() {
        DiscordEventHandlers eventHandlers = new DiscordEventHandlers();
        discordRPC.Discord_Initialize("1373993297101586462", eventHandlers, true, null);
        discordRichPresence.startTimestamp = System.currentTimeMillis() / 1000L;
        discordRichPresence.largeImageText = ClientInfo.clientName + ClientInfo.clientType;
        discordRPC.Discord_UpdatePresence(discordRichPresence);

        new Thread(() -> {
            while (true) {
                try {
                   discordRichPresence.details = "UID: " + "NE dam :)";
                   discordRichPresence.state = state;
                    discordRichPresence.largeImageKey = "https://r2.e-z.host/3b5c6e0e-0b0d-4347-b5f0-73330275f31d/dplnyr44.gif";
                    discordRPC.Discord_UpdatePresence(discordRichPresence);

                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {

                }
            }
        }).start();
    }

    public static void stopRPC() {
        discordRPC.Discord_Shutdown();
        discordRPC.Discord_ClearPresence();
    }
}