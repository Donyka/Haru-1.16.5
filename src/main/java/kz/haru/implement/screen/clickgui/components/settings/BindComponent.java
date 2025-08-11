package kz.haru.implement.screen.clickgui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import kz.haru.api.module.setting.settings.BindSetting;
import kz.haru.client.functions.KeyboardFunctions;
import kz.haru.client.functions.UpdateFunctions;
import kz.haru.implement.screen.clickgui.components.build.Component;
import kz.haru.common.utils.text.fonts.Fonts;
import kz.haru.common.utils.color.ColorUtil;
import kz.haru.common.utils.math.MouseUtil;
import kz.haru.common.utils.render.RenderUtil;
import kz.haru.common.utils.text.TextUtil;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class BindComponent extends Component {
    private final BindSetting setting;
    private boolean binding;

    public BindComponent(BindSetting setting) {
        this.setting = setting;
        setHeight(14f);
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);

        float scale = UpdateFunctions.getInstance().getScaleFactor();
        float gap = 3f * scale;
        float fontSize = 7f * scale;
        String text = binding ? "..." : KeyboardFunctions.getBind(setting.getValue());
        float textWidth = Fonts.medium.getWidth(text, fontSize);

        List<String> nameLines = TextUtil.splitText(setting.getName(), getWidth() - textWidth - gap, fontSize);
        float textY = getY() + gap + scale / 2f;

        for (String line : nameLines) {
            Fonts.medium.drawText(stack, line, getX(), textY, ColorUtil.rgb(255, 255, 255, (int) (255 * getAlpha())), fontSize);
            textY += Fonts.medium.getHeight(fontSize) + 2f * scale;
        }

        setHeight(textY - getY() + gap);

        RenderUtil.drawRound(getX() + getWidth() - textWidth - gap, getY() + 2.5f * scale, textWidth + gap * 2f, fontSize + gap, 2f * scale, ColorUtil.getClientColor(255 * getAlpha()));
        Fonts.regular.drawText(stack, text, getX() + getWidth() - textWidth, getY() + 4f * scale, ColorUtil.rgb(255, 255, 255, (int) (255 * getAlpha())), fontSize);
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        super.mouseClick(mouseX, mouseY, mouse);

        float scale = UpdateFunctions.getInstance().getScaleFactor();
        float gap = 2f * scale;
        float checkSize = 13f * scale - gap;

        if (MouseUtil.isHovered(mouseX, mouseY, getX(), getY() + gap, getWidth(), checkSize)) {
            if (mouse == 0) {
                binding = !binding;
            }
        }

        if (binding) {
            if (mouse != 0 && mouse != 1) {
                setting.setValue(mouse);
                binding = false;
            }
        }
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        super.keyPressed(key, scanCode, modifiers);

        if (binding) {
            if (key == GLFW.GLFW_KEY_DELETE) {
                setting.setValue(-1);
            } else {
                setting.setValue(key);
            }
            binding = false;
        }
    }

    @Override
    public boolean isVisible() {
        return setting.isVisible();
    }
}