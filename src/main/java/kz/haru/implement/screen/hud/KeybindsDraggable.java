package kz.haru.implement.screen.hud;

import kz.haru.api.module.Module;
import kz.haru.client.Haru;
import kz.haru.client.functions.KeyboardFunctions;
import kz.haru.client.functions.UpdateFunctions;
import kz.haru.implement.screen.hud.interfaces.ElementDraggable;
import kz.haru.common.utils.animation.AnimationUtil;
import kz.haru.common.utils.animation.Easing;
import kz.haru.common.utils.color.ColorUtil;
import kz.haru.common.utils.draggable.Draggable;
import kz.haru.common.utils.render.RenderUtil;
import kz.haru.common.utils.text.fonts.Fonts;
import kz.haru.implement.events.render.Render2DEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;

public class KeybindsDraggable extends ElementDraggable {
    public KeybindsDraggable(Draggable draggable) {
        super(draggable);
    }

    private final AnimationUtil globalAnimation = new AnimationUtil(Easing.EXPO_OUT, 300);
    private final AnimationUtil heightAnimation = new AnimationUtil(Easing.EXPO_OUT, 100);
    private final AnimationUtil widthAnimation = new AnimationUtil(Easing.EXPO_OUT, 100);

    @Override
    public void render(Render2DEvent event) {
        super.render(event);

        if (Minecraft.getInstance().gameSettings.showDebugInfo) return;

        boolean show = Minecraft.getInstance().currentScreen instanceof ChatScreen;

        String name = "Keybinds";
        float scale = UpdateFunctions.getInstance().getScaleFactor();
        float fontSize = 7.5f * scale;
        float nameWidth = Fonts.bold.getWidth(name, fontSize);
        float gap = 3f * scale;
        float width = nameWidth + gap * 4f;
        float height = fontSize + gap * 2f;
        float x = getDraggable().getX();
        float y = getDraggable().getY();

        RenderUtil.drawElementClientRect(x, y, (float) widthAnimation.getValue(), (float) heightAnimation.getValue(), (float) globalAnimation.getValue(), "");
        Fonts.bold.drawCenteredText(event.getMatrixStack(), name, (float) (x + widthAnimation.getValue() / 2f), y + fontSize / 2f - 0.5f * scale, ColorUtil.rgb(255, 255, 255, (int) (255 * globalAnimation.getValue())), fontSize);

        y += fontSize + gap;
        height += gap;

        for (Module module : Haru.getInstance().getModuleManager().getModules()) {
            float animProgress = (float) module.getAnimation().getValue();
            if (animProgress >= 0.1 && module.hasBind()) {
                show = true;
                height += fontSize;
            }

            if (animProgress <= 0.0 || !module.hasBind()) continue;

            String text = module.getName();
            String textSecond = "[" + KeyboardFunctions.getBind(module.getBind()) + "]";
            float textSecondWidth = Fonts.bold.getWidth(textSecond, fontSize);
            float localWidth = Fonts.bold.getWidth(text, fontSize) + gap * 4f + textSecondWidth;

            if (localWidth > width) {
                width = localWidth;
            }

            Fonts.bold.drawText(event.getMatrixStack(), text, x + gap, y + fontSize / 2f, ColorUtil.rgb(255, 255, 255, (int) (255 * animProgress * globalAnimation.getValue())), fontSize);
            Fonts.bold.drawText(event.getMatrixStack(), textSecond, (float) (x - gap + widthAnimation.getValue() - textSecondWidth), y + fontSize / 2f, ColorUtil.rgb(255, 255, 255, (int) (255 * animProgress * globalAnimation.getValue())), fontSize);
            y += fontSize * animProgress;
        }

        heightAnimation.run(height);
        widthAnimation.run(width);
        globalAnimation.run(show ? 1.0 : 0.0);
        getDraggable().setWidth(width);
        getDraggable().setHeight(height);
    }
}
