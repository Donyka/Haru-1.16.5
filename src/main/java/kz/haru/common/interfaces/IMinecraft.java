package kz.haru.common.interfaces;

import kz.haru.client.Haru;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;

public interface IMinecraft {

    Minecraft mc =  Minecraft.getInstance();
    MainWindow window = mc.getMainWindow();

    default void sendMessage(String message) {
        Haru.sendMessage(message);
    }
}
