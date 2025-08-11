package kz.haru.common.utils.math;

import kz.haru.common.interfaces.IMinecraft;

public class MouseUtil implements IMinecraft {
    public static boolean isHovered(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public static boolean isHovered(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= (double)x && mouseX <= (double)(x + width) && mouseY >= (double)y && mouseY <= (double)(y + height);
    }

    public static boolean isHovered(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= (double)x && mouseX <= (double)(x + width) && mouseY >= (double)y && mouseY <= (double)(y + height);
    }

    public static float getGCD() {
        float sensitivity = (float) (mc.gameSettings.mouseSensitivity * 0.6 + 0.2);
        float pow = sensitivity * sensitivity * sensitivity * 8.0f;
        return pow * 0.15f;
    }

    public static float applyGCD(float angle, float current) {
        float delta = angle - current;
        float gcd = getGCD();
        return current + (delta - (delta % gcd));
    }
}
