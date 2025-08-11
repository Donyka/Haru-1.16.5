package kz.haru.common.utils.color;

import kz.haru.common.utils.math.MathUtil;
import kz.haru.common.utils.math.Vector4i;
import kz.haru.implement.screen.clickgui.components.build.Component;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.DyeColor;
import net.minecraft.util.math.MathHelper;
import kz.haru.common.interfaces.IMinecraft;
import kz.haru.implement.modules.render.InterfaceModule;

import java.awt.*;

public class ColorUtil implements IMinecraft {
    public static int getItemColor(ItemStack stack) {
        if (stack.isEmpty()) return -1;
        
        if (stack.getItem() instanceof DyeItem) {
            DyeColor color = ((DyeItem) stack.getItem()).getDyeColor();
            float[] colorComponents = color.getColorComponentValues();
            return rgb((int) (colorComponents[0] * 255), (int) (colorComponents[1] * 255), (int) (colorComponents[2] * 255));
        }
        
        return -1;
    }

    public static int getClientColor() {
        return InterfaceModule.get().clientColor.getValue();
    }

    public static int getClientColor(float alpha) {
        return setAlpha(getClientColor(), (int) alpha);
    }

    public static int getColor(int firstColor, int secondColor, int index, float mult) {
        return ColorUtil.gradient(firstColor, secondColor, (int) (index * mult), 10);
    }

    public static int getClientColor(int index) {

        if (InterfaceModule.get().singleColor.getValue()) {
            return ColorUtil.gradient( InterfaceModule.get().clientColor.getValue(), InterfaceModule.get().clientColor.getValue(), index,10);
        } else {
            return ColorUtil.gradient( InterfaceModule.get().clientColor.getValue(), InterfaceModule.get().clientColorNext.getValue(), index,10);
        }

    }

    public static int getClientColor(int index, float alpha) {
        return ColorUtil.setAlpha(getClientColor(index), (int) alpha);
    }


    public static int gradient(int start, int end, int index, int speed) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        angle = (angle > 180 ? 360 - angle : angle) + 180;
        int color = interpolate(start, end, MathHelper.clamp(angle / 180f - 1, 0, 1));
        float[] hs = rgba(color);
        float[] hsb = Color.RGBtoHSB((int) (hs[0] * 255), (int) (hs[1] * 255), (int) (hs[2] * 255), null);

        hsb[1] *= 1.5F;
        hsb[1] = Math.min(hsb[1], 1.0f);

        return Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
    }


    public static int setAlpha(final int color, final int alpha) {
        return (MathHelper.clamp(alpha, 0, 255) << 24) | (color & 16777215);
    }

    public static float[] rgba(final int color) {
        return new float[] {(color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f};
    }

    public static int rgb(int r, int g, int b, int a) {
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static int rgb(int r, int g, int b) {
        return rgb(r, g, b, 255);
    }

    public static int toColor(String hexColor) {
        int argb = Integer.parseInt(hexColor.substring(1), 16);
        return setAlpha(argb, 255);
    }

    public static int interpolate(int start, int end, float value) {
        float[] startColor = rgba(start);
        float[] endColor = rgba(end);

        return rgb((int) MathUtil.interpolate(startColor[0] * 255, endColor[0] * 255, value),
                (int) MathUtil.interpolate(startColor[1] * 255, endColor[1] * 255, value),
                (int) MathUtil.interpolate(startColor[2] * 255, endColor[2] * 255, value),
                (int) MathUtil.interpolate(startColor[3] * 255, endColor[3] * 255, value));
    }
    
    /**
     * Manipulates a color's brightness
     * @param color Original color in ARGB format
     * @param factor Factor to multiply brightness by (0.0-1.0 for darker, >1.0 for brighter)
     * @return The adjusted color value
     */
    public static int manipulateColor(int color, float factor) {
        float[] hsb = new float[3];
        int alpha = color >> 24 & 0xFF;
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        
        Color.RGBtoHSB(r, g, b, hsb);
        // Adjust brightness
        hsb[2] = Math.min(1.0f, hsb[2] * factor);
        
        int adjustedRGB = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
        return (alpha << 24) | (adjustedRGB & 0x00FFFFFF);
    }
    
    /**
     * Applies an alpha value to a color
     * @param color Original color in RGB or ARGB format
     * @param alpha Alpha value (0-255)
     * @return The color with the specified alpha
     */
    public static int applyAlpha(int color, int alpha) {
        return (MathHelper.clamp(alpha, 0, 255) << 24) | (color & 0x00FFFFFF);
    }
}
