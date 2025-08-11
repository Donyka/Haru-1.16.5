package kz.haru.implement.screen.hud;

import kz.haru.client.functions.UpdateFunctions;
import kz.haru.implement.screen.hud.interfaces.ElementDraggable;
import kz.haru.common.utils.draggable.Draggable;
import kz.haru.common.utils.render.RenderUtil;
import kz.haru.common.utils.text.fonts.Fonts;
import kz.haru.implement.events.render.Render2DEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.NetworkPlayerInfo;

import java.awt.*;

public class PingDraggable extends ElementDraggable {
    private int currentPing = 0;
    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 500;
    
    public PingDraggable(Draggable draggable) {
        super(draggable);
    }
    
    @Override
    public void render(Render2DEvent event) {
        super.render(event);
        
        if (Minecraft.getInstance().gameSettings.showDebugInfo) return;
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().getConnection() == null) return;
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime > UPDATE_INTERVAL) {
            currentPing = getPing();
            lastUpdateTime = currentTime;
        }
        
        String prefix = "Ping: ";
        String text = prefix + currentPing;
        
        float scale = UpdateFunctions.getInstance().getScaleFactor();
        float fontSize = 7.5f * scale;
        float x = getDraggable().getX();
        float y = getDraggable().getY();
        float textWidth = Fonts.bold.getWidth(text, fontSize);
        float gap = 3f * scale;

        RenderUtil.drawElementClientRect(x, y, textWidth + gap * 2f, fontSize + gap * 2f, 1f, "");

        Fonts.bold.drawText(event.getMatrixStack(), prefix, x + gap, y + gap, -1, fontSize);
        
        float prefixWidth = Fonts.bold.getWidth(prefix, fontSize);
        Fonts.bold.drawText(event.getMatrixStack(), String.valueOf(currentPing), x + gap + prefixWidth, y + gap, getPingColor(currentPing), fontSize);

        getDraggable().setHeight(fontSize + gap * 2f);
        getDraggable().setWidth(textWidth + gap * 2f);
    }
    
    private int getPing() {
        NetworkPlayerInfo info = Minecraft.getInstance().getConnection().getPlayerInfo(Minecraft.getInstance().player.getGameProfile().getId());
        return info != null ? info.getResponseTime() : 0;
    }
    
    private int getPingColor(int ping) {
        if (ping < 55) {
            return new Color(0, 255, 0).getRGB();
        } else if (ping < 110) {
            return new Color(255, 255, 0).getRGB();
        } else if (ping < 140) {
            return new Color(255, 165, 0).getRGB();
        } else {
            return new Color(255, 0, 0).getRGB();
        }
    }
}