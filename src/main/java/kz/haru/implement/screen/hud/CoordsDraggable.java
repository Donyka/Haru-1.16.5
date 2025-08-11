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

import static kz.haru.common.interfaces.IMinecraft.mc;

public class CoordsDraggable extends ElementDraggable {
    
    public CoordsDraggable(Draggable draggable) {
        super(draggable);
    }
    
    @Override
    public void render(Render2DEvent event) {
        super.render(event);
        
        if (Minecraft.getInstance().gameSettings.showDebugInfo) return;
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().getConnection() == null) return;

        
        String prefix = "XYZ: ";
        String coord = mc.player.getPosition().getX() + ", " + mc.player.getPosition().getY() + ", " + mc.player.getPosition().getZ();
        String text = prefix + coord;
        
        float scale = UpdateFunctions.getInstance().getScaleFactor();
        float fontSize = 7.5f * scale;
        float x = getDraggable().getX();
        float y = getDraggable().getY();
        float textWidth = Fonts.bold.getWidth(text, fontSize);
        float gap = 3f * scale;

        RenderUtil.drawElementClientRect(x, y, textWidth + gap * 2f, fontSize + gap * 2f, 1f, "");

        Fonts.bold.drawText(event.getMatrixStack(), prefix, x + gap, y + gap, -1, fontSize);
        
        float prefixWidth = Fonts.bold.getWidth(prefix, fontSize);
        Fonts.bold.drawText(event.getMatrixStack(), coord, x + gap + prefixWidth, y + gap, -1, fontSize);

        getDraggable().setHeight(fontSize + gap * 2f);
        getDraggable().setWidth(textWidth + gap * 2f);
    }

}