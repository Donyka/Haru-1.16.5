package kz.haru.implement.screen.hud;

import kz.haru.client.functions.UpdateFunctions;
import kz.haru.implement.screen.hud.interfaces.ElementDraggable;
import kz.haru.common.utils.client.TpsUtil;
import kz.haru.common.utils.color.ColorUtil;
import kz.haru.common.utils.draggable.Draggable;
import kz.haru.common.utils.render.RenderUtil;
import kz.haru.common.utils.text.fonts.Fonts;
import kz.haru.implement.events.render.Render2DEvent;
import net.minecraft.client.Minecraft;

import java.text.DecimalFormat;

public class TpsDraggable extends ElementDraggable {
    private final DecimalFormat df = new DecimalFormat("##.##");
    private float lastTps = 20.0f;
    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 1000;
    
    public TpsDraggable(Draggable draggable) {
        super(draggable);
    }
    
    @Override
    public void render(Render2DEvent event) {
        super.render(event);
        
        if (Minecraft.getInstance().gameSettings.showDebugInfo) return;
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime > UPDATE_INTERVAL) {
            float currentTps = TpsUtil.getTickRate();
            if (currentTps > 0) {
                lastTps = currentTps;
            }
            lastUpdateTime = currentTime;
        }
        
        String prefix = "TPS: ";
        String value = df.format(lastTps);
        String text = prefix + value;
        
        float scale = UpdateFunctions.getInstance().getScaleFactor();
        float fontSize = 7.5f * scale;
        float x = getDraggable().getX();
        float y = getDraggable().getY();
        float textWidth = Fonts.bold.getWidth(text, fontSize);
        float gap = 3f * scale;

        RenderUtil.drawElementClientRect(x, y, textWidth + gap * 2f, fontSize + gap * 2f, 1f, "");

        Fonts.bold.drawText(event.getMatrixStack(), prefix, x + gap, y + gap, -1, fontSize);
        
        float prefixWidth = Fonts.bold.getWidth(prefix, fontSize);
        Fonts.bold.drawText(event.getMatrixStack(), value, x + gap + prefixWidth, y + gap, getTpsColor(lastTps), fontSize);
        
        getDraggable().setHeight(fontSize + gap * 2f);
        getDraggable().setWidth(textWidth + gap * 2f);
    }
    
    private int getTpsColor(float tps) {
        return ColorUtil.interpolate(ColorUtil.rgb(0, 255, 0), ColorUtil.rgb(255, 0, 0), tps / 20f);
    }
}