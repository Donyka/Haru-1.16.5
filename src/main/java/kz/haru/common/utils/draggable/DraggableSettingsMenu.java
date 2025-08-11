package kz.haru.common.utils.draggable;

import com.mojang.blaze3d.matrix.MatrixStack;
import kz.haru.api.module.setting.Setting;
import kz.haru.api.module.setting.settings.*;
import kz.haru.client.functions.UpdateFunctions;
import kz.haru.common.interfaces.IMinecraft;
import kz.haru.common.utils.animation.AnimationUtil;
import kz.haru.common.utils.animation.Easing;
import kz.haru.common.utils.color.ColorUtil;
import kz.haru.common.utils.render.RenderUtil;
import kz.haru.common.utils.text.fonts.Fonts;
import kz.haru.implement.screen.clickgui.components.settings.*;
import kz.haru.implement.screen.clickgui.components.build.Component;
import net.minecraft.client.gui.screen.ChatScreen;

import java.util.ArrayList;
import java.util.List;

public class DraggableSettingsMenu implements IMinecraft {
    private final Draggable draggable;
    private final List<Component> components = new ArrayList<>();
    private final AnimationUtil scaleAnimation = new AnimationUtil(Easing.SHRINK, 300);
    private final AnimationUtil alphaAnimation = new AnimationUtil(Easing.CUBIC_OUT, 250);

    public DraggableSettingsMenu(Draggable draggable) {
        this.draggable = draggable;

        for (Setting<?> setting : draggable.getSettings()) {
            if (setting instanceof ModeSetting modeSetting) {
                components.add(new ModeComponent(modeSetting));
            }
            if (setting instanceof FloatSetting floatSetting) {
                components.add(new FloatComponent(floatSetting));
            }
            if (setting instanceof BooleanSetting booleanSetting) {
                components.add(new BooleanComponent(booleanSetting));
            }
            if (setting instanceof MultiModeSetting multiModeSetting) {
                components.add(new MultiModeComponent(multiModeSetting));
            }
            if (setting instanceof BindSetting bindSetting) {
                components.add(new BindComponent(bindSetting));
            }
            if (setting instanceof ColorSetting colorSetting) {
                components.add(new ColorComponent(colorSetting));
            }
        }
    }

    public void render(MatrixStack matrixStack) {
        if (draggable.getSettings().isEmpty() /*|| draggable.isDragging()*/ || !(mc.currentScreen instanceof ChatScreen)) {
            return;
        }

        scaleAnimation.setEasing(draggable.isSettingsShown() && !draggable.isDragging() ? Easing.SHRINK : Easing.CUBIC_IN);
        scaleAnimation.run(draggable.isSettingsShown() && !draggable.isDragging() ? 1.0 : 0.0);

        alphaAnimation.setEasing(draggable.isSettingsShown() && !draggable.isDragging() ? Easing.CUBIC_OUT : Easing.CUBIC_IN);
        alphaAnimation.run(draggable.isSettingsShown() && !draggable.isDragging() ? 1.0 : 0.0);

        if (scaleAnimation.getValue() <= 0.01 && !draggable.isSettingsShown()) {
            return;
        }

        float scale = UpdateFunctions.getInstance().getScaleFactor();
        float padding = 2f * scale;
        float menuWidth = 110f * scale;
        float headerHeight = 14f * scale;

        float scaleValue = (float)scaleAnimation.getValue();
        float alphaValue = (float)alphaAnimation.getValue();

        float menuX = draggable.getX() + draggable.getWidth() + 5f * scale;
        float fullMenuY = draggable.getY();
        float menuOffset = (1f - scaleValue) * 20f * scale;
        float menuY = fullMenuY + menuOffset;

        float currentY = menuY + headerHeight + padding / 2;
        float maxHeight = headerHeight + padding / 2;

        for (Component component : components) {
            if (component.isVisible()) {
                component.setX(menuX + padding);
                component.setY(currentY);
                component.setWidth(menuWidth - padding * 2);
                component.setAlpha(alphaValue);

                currentY += component.getHeight() + padding / 2;
                maxHeight += component.getHeight() + padding / 2;
            }
        }

        float menuHeight = maxHeight + padding / 2;

        int bgColor = ColorUtil.rgb(15, 15, 15, (int)(200 * alphaValue));
        RenderUtil.drawRound(menuX, menuY, menuWidth, maxHeight, 4, bgColor);


        String title = draggable.getName();
        float titleWidth = Fonts.bold.getWidth(title, 8f * scale);
        float titleX = menuX + (menuWidth - titleWidth) / 2;
        float titleY = menuY + padding / 2 + 2.5f;

        Fonts.bold.drawText(
                matrixStack,
                title,
                titleX,
                titleY,
                ColorUtil.rgb(255, 255, 255, (int)(255 * alphaValue)),
                8f * scale
        );

        //RenderUtil.drawRound(menuX + padding, menuY + headerHeight - 2f * scale, (menuWidth - padding * 2), 1f * scale, 0.5f * scale, ColorUtil.getClientColor((int)(150 * alphaValue)));

        for (Component component : components) {
            if (component.isVisible()) {
                component.render(matrixStack, menuX, menuY);
            }
        }
    }

    public void mouseClicked(float mouseX, float mouseY, int button) {
        if (draggable.isSettingsShown() || draggable.getSettings().isEmpty() || button != 0 || scaleAnimation.getValue() < 0.8) {
            return;
        }

        if (draggable.isDragging() || !(mc.currentScreen instanceof ChatScreen)) {
            return;
        }

        for (Component component : components) {
            if (component.isVisible()) {
                component.mouseClick(mouseX, mouseY, button);
            }
        }
    }

    private float calcMenuHeight() {
        float scale = UpdateFunctions.getInstance().getScaleFactor();
        float padding = 2f * scale;
        float headerHeight = 14f * scale;
        float height = headerHeight + padding;

        for (Component component : components) {
            height += component.getHeight() + padding / 2;
        }

        return height + padding / 2;
    }

    public void mouseReleased(float mouseX, float mouseY, int button) {
        if (!draggable.isSettingsShown() || draggable.getSettings().isEmpty() || scaleAnimation.getValue() < 0.8) {
            return;
        }

        if (draggable.isDragging() || !(mc.currentScreen instanceof ChatScreen)) {
            return;
        }
        
        for (Component component : components) {
            component.mouseRelease(mouseX, mouseY, button);
        }
    }
} 