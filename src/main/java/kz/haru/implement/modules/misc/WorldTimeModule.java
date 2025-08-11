package kz.haru.implement.modules.misc;

import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.module.setting.settings.ModeSetting;
import kz.haru.implement.events.connection.HPacketEvent;
import net.minecraft.network.play.server.SUpdateTimePacket;
import java.util.Calendar;

@ModuleRegister(name = "World Time", category = Category.MISC, desc = "Made In Heaven?")
public class WorldTimeModule extends Module {
    private final ModeSetting timeMode = new ModeSetting("Time mode").value("Noon").addValues("Sunrise", "Noon", "Sunset", "Midnight", "Day", "Night", "Real Time");

    public WorldTimeModule() {
        setup(timeMode);
    }

    @EventTarget
    public void onPacket(HPacketEvent event) {
        if (event.isReceive() && event.getPacket() instanceof SUpdateTimePacket packet) {
            switch (timeMode.getValue()) {
                case "Sunrise":
                    packet.setWorldTime(0);
                    break;
                case "Noon":
                    packet.setWorldTime(6000);
                    break;
                case "Sunset":
                    packet.setWorldTime(12000);
                    break;
                case "Midnight":
                    packet.setWorldTime(18000);
                    break;
                case "Day":
                    packet.setWorldTime(1000);
                    break;
                case "Night":
                    packet.setWorldTime(13000);
                    break;
                case "Real Time":
                    Calendar calendar = Calendar.getInstance();
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);
                    int totalMinutes = hour * 60 + minute;
                    long mcTime = (long) (((totalMinutes - 360 + 1440) % 1440) * (24000.0 / 1440));
                    packet.setWorldTime(mcTime);
                    break;
            }
        }
    }
}