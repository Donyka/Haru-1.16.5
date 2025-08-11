package kz.haru.implement.screen.clickgui.components.build;

import com.mojang.blaze3d.matrix.MatrixStack;

public interface IComponent {
    default void render(MatrixStack stack, float mouseX, float mouseY) {}
    default void mouseClick(float mouseX, float mouseY, int mouse) {}
    default void charTyped(char codePoint, int modifiers) {}
    default void mouseRelease(float mouseX, float mouseY, int mouse) {}
    default void keyPressed(int key, int scanCode, int modifiers) {}
}
