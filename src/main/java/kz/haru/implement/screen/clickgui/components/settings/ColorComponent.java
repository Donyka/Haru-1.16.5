package kz.haru.implement.screen.clickgui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import kz.haru.api.module.setting.settings.ColorSetting;
import kz.haru.client.functions.UpdateFunctions;
import kz.haru.common.utils.animation.AnimationUtil;
import kz.haru.common.utils.animation.Easing;
import kz.haru.common.utils.math.Vector4i;
import kz.haru.implement.screen.clickgui.components.build.Component;
import kz.haru.common.utils.color.ColorUtil;
import kz.haru.common.utils.math.MouseUtil;
import kz.haru.common.utils.render.RenderUtil;
import kz.haru.common.utils.text.fonts.Fonts;
import net.minecraft.util.math.vector.Vector4f;

import java.awt.Color;

public class ColorComponent extends Component {
    public final ColorSetting setting;
    private boolean open;
    
    private boolean draggingHue = false;
    private boolean draggingSaturation = false;
    
    private float hue;
    private float saturation;
    private float brightness;
    private float alpha;
    private boolean isFirst = true;
    public final AnimationUtil animation = new AnimationUtil(Easing.EXPO_OUT, 350);
    
    public ColorComponent(ColorSetting setting) {
        this.setting = setting;
        setHeight(14f);
        
        Color color = new Color(setting.getValue(), true);
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
        this.alpha = color.getAlpha() / 255f;
    }
    
    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);

        animation.run(open ? 1.0 : 0.0);
        
        float scale = UpdateFunctions.getInstance().getScaleFactor();
        float gap = 2f * scale;
        
        Fonts.regular.drawText(stack, setting.getName(), getX() + gap, getY() + gap * 1.7f, ColorUtil.rgb(255, 255, 255, (int) (255 * getAlpha())), 7.5f * scale);
        
        float colorBoxSize = 10f * scale;
        RenderUtil.drawRound(getX() + getWidth() - colorBoxSize - gap, getY() + gap, colorBoxSize, colorBoxSize, 2f * scale, ColorUtil.setAlpha(setting.getValue(), (int) (255 * getAlpha())));
        
        if (animation.getValue() > 0.0) {
            float sex = (float) animation.getValue();
            float pickerSize = getWidth();
            float pickerX = getX();
            float pickerY = getY() + 14f * scale - (3f * scale) * (1f - sex);

            float saturationPickerSize = pickerSize;
            float saturationPickerHeight = (pickerSize * sex);
            float saturationPickerY = pickerY;

            if (!isFirst) {
                if (draggingSaturation) {
                    updateSaturationBrightness(mouseX, mouseY, pickerX, saturationPickerY, saturationPickerSize);
                }

                if (draggingHue) {
                    updateHue(mouseX, pickerX, pickerSize);
                }
            }

            Color hueColor = Color.getHSBColor(hue, 1.0f, 1.0f);

            int topLeftColor = ColorUtil.rgb(255, 255, 255, (int) (255 * sex * getAlpha()));
            int topRightColor = ColorUtil.rgb(hueColor.getRed(), hueColor.getGreen(), hueColor.getBlue(), (int) (255 * sex * getAlpha()));
            int bottomLeftColor = ColorUtil.rgb(0, 0, 0, (int) (255 * sex * getAlpha()));
            int bottomRightColor = ColorUtil.rgb(0, 0, 0, (int) (255 * sex * getAlpha()));

            RenderUtil.drawRound(pickerX, saturationPickerY, saturationPickerSize, saturationPickerHeight, new Vector4f(5f * scale), 1f, new Vector4i(topLeftColor, bottomRightColor, topRightColor, bottomLeftColor));

            float satX = pickerX + saturation * saturationPickerSize;
            float satY = saturationPickerY + (1 - brightness) * saturationPickerHeight;

            float circleSize = 5f * scale;
            float halfCircle = circleSize / 2f;

            float borderOffset = 5f * scale;
            satX = Math.max(pickerX + borderOffset, Math.min(pickerX + saturationPickerSize - borderOffset, satX));
            satY = Math.max(saturationPickerY + borderOffset, Math.min(saturationPickerY + saturationPickerHeight - borderOffset, satY));

            RenderUtil.drawRound(satX - halfCircle, satY - halfCircle, circleSize, circleSize, halfCircle, ColorUtil.rgb(255, 255, 255, (int) (255 * sex * getAlpha())));

            float huePickerHeight = 10f * scale * sex;
            float huePickerY = saturationPickerY + saturationPickerHeight + gap;

            float huiOffset = 3f * scale;
            float huePickerX = pickerX + huiOffset;
            float huePickerWidth = saturationPickerSize - huiOffset * 2f;

            RenderUtil.drawRound(huePickerX, huePickerY, huePickerWidth, huePickerHeight, 3f * scale, ColorUtil.rgb(25, 25, 25, (int)(150 * sex * getAlpha())));

            int segments = 100;
            float overlap = 8.0f;

            for (int i = 0; i < segments; i++) {
                float hueVal = 0.01f + (i / (float) segments) * 0.98f;
                Color hueSegColor = Color.getHSBColor(hueVal, 1f, 1f);
                int currentColor = ColorUtil.rgb(hueSegColor.getRed(), hueSegColor.getGreen(), hueSegColor.getBlue(), (int)(125 * sex * getAlpha()));

                float segWidth = (huePickerWidth / segments) * overlap;
                float segX = huePickerX + (i * huePickerWidth / segments) - (segWidth - huePickerWidth/segments) / 2;

                RenderUtil.drawRound(segX, huePickerY, segWidth, huePickerHeight, 3f * scale, currentColor);
            }

            RenderUtil.drawRound(huePickerX, huePickerY, huePickerWidth, huePickerHeight, 3f * scale, 0.5f, ColorUtil.rgb(40, 40, 40, (int)(40 * sex * getAlpha())));

            float hueX = huePickerX + hue * huePickerWidth;

            float hueCircleSize = 6f * scale;
            float borderRadiusOffset = 3f * scale;
            hueX = Math.max(huePickerX + borderRadiusOffset, Math.min(huePickerX + huePickerWidth - borderRadiusOffset, hueX));

            RenderUtil.drawRound(hueX - hueCircleSize / 2f, huePickerY + huePickerHeight / 2f - hueCircleSize / 2f,
                               hueCircleSize, hueCircleSize, hueCircleSize / 2f,
                               ColorUtil.rgb(255, 255, 255, (int) (255 * sex * getAlpha())));

            float moreSex = gap * sex;
            setHeight(14f * scale + saturationPickerHeight + moreSex + huePickerHeight + moreSex);

            int argb = Color.HSBtoRGB(hue, saturation, brightness);
            Color rgbColor = new Color(argb);
            setting.value(ColorUtil.rgb(rgbColor.getRed(), rgbColor.getGreen(), rgbColor.getBlue(), (int) (alpha * 255)));
        } else {
            setHeight(14f * scale);
        }
    }
    
    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        super.mouseClick(mouseX, mouseY, mouse);

        if (mouse == 1) {
            float scale = UpdateFunctions.getInstance().getScaleFactor();
            float gap = 2f * scale;
            float colorBoxSize = 10f * scale;
            
            if (MouseUtil.isHovered(mouseX, mouseY, getX() + getWidth() - colorBoxSize - gap, getY() + gap, colorBoxSize, colorBoxSize)) {
                open = !open;
                return;
            }
        }

        if (mouse == 0) {
            float scale = UpdateFunctions.getInstance().getScaleFactor();
            float gap = 2f * scale;
            
            if (animation.getValue() > 0.9) {
                float pickerSize = getWidth();
                float pickerX = getX();
                float pickerY = getY() + 14f;
                
                float saturationPickerSize = pickerSize;
                float saturationPickerY = pickerY;
                
                if (MouseUtil.isHovered(mouseX, mouseY, pickerX, saturationPickerY, saturationPickerSize, saturationPickerSize)) {
                    draggingSaturation = true;
                    isFirst = false;
                    updateSaturationBrightness(mouseX, mouseY, pickerX, saturationPickerY, saturationPickerSize);
                }
                
                float huePickerHeight = 10f * scale;
                float huePickerY = saturationPickerY + saturationPickerSize + gap;
                float huePickerX = pickerX;
                float huePickerWidth = saturationPickerSize;
                
                if (MouseUtil.isHovered(mouseX, mouseY, huePickerX, huePickerY, huePickerWidth, huePickerHeight)) {
                    draggingHue = true;
                    isFirst = false;
                    updateHue(mouseX, huePickerX, huePickerWidth);
                }
            }
        }
    }
    
    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        super.mouseRelease(mouseX, mouseY, mouse);
        
        draggingHue = false;
        draggingSaturation = false;
    }
    
    private void updateSaturationBrightness(float mouseX, float mouseY, float pickerX, float pickerY, float pickerSize) {
        float rawSaturation = (mouseX - pickerX) / pickerSize;
        float rawBrightness = 1 - (mouseY - pickerY) / pickerSize;
        
        saturation = Math.max(0, Math.min(1, rawSaturation));
        brightness = Math.max(0, Math.min(1, rawBrightness));
    }
    
    private void updateHue(float mouseX, float pickerX, float pickerSize) {
        float rawHue = (mouseX - pickerX) / pickerSize;
        hue = Math.max(0, Math.min(1, rawHue));
    }
    
    @Override
    public boolean isVisible() {
        return setting.isVisible();
    }
}