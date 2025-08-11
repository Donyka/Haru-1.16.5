package kz.haru.implement.screen.hud;

import kz.haru.api.module.setting.settings.ModeSetting;
import kz.haru.client.ClientInfo;
import kz.haru.client.functions.UpdateFunctions;
import kz.haru.common.utils.draggable.Draggable;
import kz.haru.common.utils.draggable.DraggableSettingsMenu;
import kz.haru.common.utils.text.fonts.Fonts;
import kz.haru.implement.screen.hud.interfaces.ElementDraggable;
import kz.haru.common.utils.render.RenderUtil;
import kz.haru.implement.events.render.Render2DEvent;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.matrix.MatrixStack;

public class WatermarkDraggable extends ElementDraggable {
    private final DraggableSettingsMenu settingsMenu;
    private final ModeSetting watermarkType = new ModeSetting("Type").value("Default").addValues("Default", "Minimal", "Extended");

    public WatermarkDraggable(Draggable draggable) {
        super(draggable);
        draggable.addSettings(watermarkType);
        this.settingsMenu = new DraggableSettingsMenu(draggable);
    }

    @Override
    public void render(Render2DEvent event) {
        super.render(event);

        if (Minecraft.getInstance().gameSettings.showDebugInfo) return;

        String text;
        switch (watermarkType.getValue()) {
            case "Minimal":
                text = ClientInfo.clientName;
                break;
            case "Extended":
                text = ClientInfo.clientName + " " + ClientInfo.clientType + " / " + Minecraft.debugFPS + "fps";
                break;
            case "Default":
            default:
                text = ClientInfo.clientName + " / " + Minecraft.debugFPS + "fps";
                break;
        }
        float scale = UpdateFunctions.getInstance().getScaleFactor();
        float fontSize = 7.5f * scale;
        float x = getDraggable().getX();
        float y = getDraggable().getY();
        float textWidth = Fonts.bold.getWidth(text, fontSize);
        float gap = 3f * scale;

        RenderUtil.drawElementClientRect(x, y, textWidth + gap * 2f, fontSize + gap * 2f, 1f, "");
        Fonts.bold.drawText(event.getMatrixStack(), text, x + gap, y + gap, -1, fontSize);

        getDraggable().setHeight(fontSize + gap * 2f);
        getDraggable().setWidth(textWidth + gap * 2f);

        // Render settings menu
        settingsMenu.render(event.getMatrixStack());
    }

    @Override
    public void mouseClicked(float mouseX, float mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        settingsMenu.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public void mouseReleased(float mouseX, float mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        settingsMenu.mouseReleased(mouseX, mouseY, button);
    }
}
