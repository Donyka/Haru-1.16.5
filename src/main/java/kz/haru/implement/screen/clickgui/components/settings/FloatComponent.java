package kz.haru.implement.screen.clickgui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import kz.haru.api.module.setting.settings.FloatSetting;
import kz.haru.client.functions.UpdateFunctions;
import kz.haru.common.utils.math.MathUtil;
import kz.haru.common.utils.math.Vector4i;
import kz.haru.implement.screen.clickgui.components.build.Component;
import kz.haru.common.utils.animation.AnimationUtil;
import kz.haru.common.utils.animation.Easing;
import kz.haru.common.utils.color.ColorUtil;
import kz.haru.common.utils.math.MouseUtil;
import kz.haru.common.utils.render.RenderUtil;
import kz.haru.common.utils.text.TextUtil;
import kz.haru.common.utils.text.fonts.Fonts;
import net.minecraft.util.math.vector.Vector4f;

import java.util.List;

public class FloatComponent extends Component {
    public final FloatSetting setting;
    private boolean isDragging = false;
    private float sliderWidth, sliderHeight;
    private final AnimationUtil animation = new AnimationUtil(Easing.LINEAR, 100);
    public float lastValue;

    public FloatComponent(FloatSetting setting) {
        this.setting = setting;
        setHeight(20f);
        lastValue = setting.getValue();
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        float scale = UpdateFunctions.getInstance().getScaleFactor();
        float padding = 3f * scale;

        setHeight(20f * scale);
        float sliderY = getY() + 12f * scale;

        String valueText = String.valueOf(MathUtil.round(lastValue, setting.getStep()));
        String valueMinText = String.valueOf(MathUtil.round(setting.getMin(), setting.getStep()));
        String valueMaxText = String.valueOf(MathUtil.round(setting.getMax(), setting.getStep()));
        float valueWidth = Fonts.medium.getWidth(valueMaxText, 7f * scale);

        List<String> nameLines = TextUtil.splitText(setting.getName(), getWidth(), 7f * scale);
        float textY = getY();

        for (String line : nameLines) {
            Fonts.medium.drawText(stack, line, getX(), textY + padding / 2f, ColorUtil.rgb(255, 255, 255, (int) (255 * getAlpha())), 7f * scale);
            textY += Fonts.medium.getHeight(7f * scale) + 2f * scale;
        }

        sliderY = textY + 0.5f * scale;

        this.sliderWidth = getWidth();
        this.sliderHeight = 4f * scale;

        RenderUtil.drawRound(getX(), sliderY, sliderWidth, sliderHeight, sliderHeight / 2.5f, ColorUtil.rgb(45, 45, 45, (int) (255 * getAlpha())));

        float progressWidth = (lastValue - setting.getMin()) / (setting.getMax() - setting.getMin()) * sliderWidth;
        animation.run(progressWidth);
        float animProgressWidth = (float) animation.getValue();

        int alpha = (int) (255 * getAlpha());
        Vector4i color = new Vector4i(ColorUtil.getClientColor(90, alpha), ColorUtil.getClientColor(90, alpha), ColorUtil.getClientColor(180, alpha), ColorUtil.getClientColor(180, alpha));

        RenderUtil.drawRound(getX(), sliderY, animProgressWidth, sliderHeight, new Vector4f(sliderHeight / 2.5f,sliderHeight / 2.5f,sliderHeight / 2.5f,sliderHeight / 2.5f),0.05f, color);

        float sliderPos = getX() + animProgressWidth;
        float knobSize = sliderHeight + scale;
        RenderUtil.drawRound(sliderPos - knobSize / 2f, sliderY - (knobSize - sliderHeight) / 2f, knobSize, knobSize, knobSize / 2.5f, ColorUtil.rgb(255, 255, 255, (int) (255 * getAlpha())));

        Fonts.regular.drawCenteredText(stack, valueText, getX() + getWidth() / 2f, sliderY + knobSize + scale, ColorUtil.rgb(170, 170, 170, (int) (255 * getAlpha())), 7f * scale);
        Fonts.regular.drawText(stack, valueMinText, getX(), sliderY + knobSize + scale, ColorUtil.rgb(170, 170, 170, (int) (255 * getAlpha())), 7f * scale);
        Fonts.regular.drawText(stack, valueMaxText, getX() + getWidth() - valueWidth, sliderY + knobSize + scale, ColorUtil.rgb(170, 170, 170, (int) (255 * getAlpha())), 7f * scale);


        if (isDragging) {
            float newValue = (mouseX - (getX() + padding)) / sliderWidth;
            newValue = setting.getMin() + newValue * (setting.getMax() - setting.getMin());
            newValue = Math.round(newValue / setting.getStep()) * setting.getStep();
            lastValue = (float) MathUtil.round((Math.max(setting.getMin(), Math.min(setting.getMax(), newValue))), setting.getStep());

            if (setting.isRun()) {
                setting.setValue(lastValue);
            }
        } else {
            if (!setting.isRun() && setting.getValue() != lastValue) {
                setting.setValue(lastValue);
            }
        }
        setHeight(textY - getY() + 10f * scale + knobSize + scale);
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        super.mouseClick(mouseX, mouseY, mouse);
        float scale = UpdateFunctions.getInstance().getScaleFactor();
        float padding = 3f * scale;
        float sliderY = getY() + 12f * scale;
        List<String> nameLines = TextUtil.splitText(setting.getName(), getWidth() - Fonts.bold.getWidth(String.format("%.2f", setting.getMax()).replace(",", "."), 7f * scale), 7f * scale);
        float textY = getY();

        for (String line : nameLines) {
            textY += Fonts.bold.getHeight(7f * scale) + 2f * scale;
        }

        sliderY = textY + 0.5f * scale;

        if (MouseUtil.isHovered(mouseX, mouseY, getX() + padding, sliderY, sliderWidth, sliderHeight)) {
            if (mouse == 0) {
                isDragging = true;
            }
        }
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        super.mouseRelease(mouseX, mouseY, mouse);
        isDragging = false;
    }

    @Override
    public boolean isVisible() {
        return setting.isVisible();
    }
}
