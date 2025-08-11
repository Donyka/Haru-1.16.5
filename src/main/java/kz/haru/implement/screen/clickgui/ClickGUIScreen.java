package kz.haru.implement.screen.clickgui;

import com.mojang.blaze3d.matrix.MatrixStack;
import kz.haru.api.module.Category;
import kz.haru.common.config.clickgui.ConfigClickGui;
import kz.haru.client.functions.UpdateFunctions;
import kz.haru.common.interfaces.IMinecraft;
import kz.haru.common.utils.color.ColorUtil;
import kz.haru.implement.modules.render.ClickGUIModule;
import kz.haru.implement.screen.clickgui.components.ModuleComponent;
import kz.haru.common.utils.animation.AnimationUtil;
import kz.haru.common.utils.animation.Easing;
import kz.haru.common.utils.render.RenderUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClickGUIScreen extends Screen implements IMinecraft {
    public final List<Panel> panels = new ArrayList<>();

    public static final AnimationUtil openCloseAnim = new AnimationUtil(Easing.EXPO_OUT, 500);
    public static final AnimationUtil alphaOpenCloseAnim = new AnimationUtil(Easing.EXPO_OUT, 500);
    private final AnimationUtil scrollAnim = new AnimationUtil(Easing.EXPO_OUT, 600);
    public static ModuleComponent expandedModule = null;

    public static boolean isExit;
    private float scroll = 0f;
    public float yPanel = 3f;

    public ClickGUIScreen(ITextComponent titleIn) {
        super(titleIn);

        Category[] categories = Category.values();
        for (Category category : categories) {
            panels.add(new Panel(category));
        }
    }

    @Override
    protected void init() {
        super.init();
        isExit = false;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        openCloseAnim.setDuration(!isExit ? 300 : 400);
        openCloseAnim.setEasing(!isExit ? Easing.BACK_OUT : Easing.BACK_IN);
        openCloseAnim.run(!isExit ? 1.0 : 0.0);

        alphaOpenCloseAnim.setDuration(!isExit ? 600 : 1250);
        alphaOpenCloseAnim.run(!isExit ? 1.0 : 0.0);

        if (alphaOpenCloseAnim.getValue() <= 0.1 && isExit) {
            closeScreen();
        }

        float windowWidth = window.getScaledWidth();
        float off = 8f * UpdateFunctions.getInstance().getScaleFactor();
        scrollAnim.run(scroll);

        float totalWidth = panels.stream()
                .map(Panel::getWidth)
                .reduce(0f, Float::sum) + (panels.size() - 1) * (off / 2f);

        float maxHeight = panels.stream()
                .map(Panel::getHeight)
                .max(Float::compare)
                .orElse(0f);

        float firstPanelX = (windowWidth - totalWidth) / 2f;
        float scroll = (float) scrollAnim.getValue();
        float scaleCenterX = firstPanelX + totalWidth / 2f;
        float scaleCenterY = yPanel + scroll + maxHeight / 2f;

        if (ClickGUIModule.background.getAnimation().getValue() > 0.0) {
            float alpha = (float) alphaOpenCloseAnim.getValue();
            float bgAnim = (float) ClickGUIModule.background.getAnimation().getValue();
            float normalAnim = MathHelper.clamp(bgAnim * alpha, 0f, 1f);

            RenderUtil.drawRound(0f, 0f, windowWidth, window.getScaledHeight(), 0f, ColorUtil.rgb(0, 0, 0, (int) (ClickGUIModule.backgroundAlpha.getValue() * normalAnim)));
        }

        RenderUtil.scaleStart(scaleCenterX, scaleCenterY + 100f * UpdateFunctions.getInstance().getScaleFactor(), (float) openCloseAnim.getValue());

        for (Panel panel : panels) {
            panel.setAlpha((float) (alphaOpenCloseAnim.getValue()));
            panel.setY((yPanel + scroll));
            panel.setX((firstPanelX + panels.indexOf(panel) * (panel.getWidth() + (off / 2f))));

            panel.render(matrixStack, (float) mouseX, (float) mouseY);
        }

        if (ClickGUIModule.descriptions.getValue()) {
            for (Panel panel : panels) {
                panel.drawDesc(matrixStack, mouseX, mouseY);
            }
        }

        RenderUtil.scaleStop();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {

            panels.forEach(panel -> {
                panel.getModuleComponents().forEach(moduleComponent -> {
                    moduleComponent.setBind(false);
                    moduleComponent.mouseRelease(0f, 0f, 0);
                });
            });

            mc.mouseHelper.grabMouse(false);
            kz.haru.implement.modules.render.ClickGUIModule.get().setEnabled(false);
            isExit = true;

            return true;
        }

        panels.forEach(panel -> panel.keyPressed(keyCode, scanCode, modifiers));

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        panels.forEach(panel -> panel.mouseClick((float) mouseX, (float) mouseY, button));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scroll -= (float) (delta * 20f);

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        panels.forEach(panel -> panel.mouseRelease((float) mouseX, (float) mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void closeScreen() {
        super.closeScreen();
        ConfigClickGui.savePanelPositions();
    }

    private void loadPanelPositions() {

    }
}
