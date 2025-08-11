package kz.haru.implement.screen.clickgui;

import com.mojang.blaze3d.matrix.MatrixStack;
import kz.haru.api.module.Category;
import kz.haru.client.Haru;
import kz.haru.client.functions.UpdateFunctions;
import kz.haru.common.utils.animation.AnimationUtil;
import kz.haru.common.utils.animation.Easing;
import kz.haru.common.utils.math.MouseUtil;
import kz.haru.common.utils.text.TextUtil;
import kz.haru.implement.screen.clickgui.components.ModuleComponent;
import kz.haru.implement.screen.clickgui.components.build.Component;
import kz.haru.common.utils.color.ColorUtil;
import kz.haru.common.utils.render.RenderUtil;
import kz.haru.common.utils.text.fonts.Fonts;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Panel extends Component {
    private final Category category;
    private final List<ModuleComponent> moduleComponents = new ArrayList<>();
    private float scroll;
    private ModuleComponent moduleForDesc = null;
    private ModuleComponent lastModuleForDesc = null;
    private final AnimationUtil descAnimation = new AnimationUtil(Easing.EXPO_OUT, 200);


    public Panel(Category category) {
        this.category = category;

        Haru.getInstance().getModuleManager().getModules().forEach(m -> {
            if (m.getCategory() == category) {
                ModuleComponent moduleComponent = new ModuleComponent(m);
                moduleComponent.setPanel(this);
                moduleComponents.add(moduleComponent);
            }
        });

        if (!moduleComponents.isEmpty()) {
            moduleComponents.get(moduleComponents.size() - 1).setLast(true);
        }

        setWidth(105f * UpdateFunctions.getInstance().getScaleFactor());
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);

        float scale = UpdateFunctions.getInstance().getScaleFactor();
        float gap = 3f * scale;
        float header = 18f * scale;
        float offset = header;
        setWidth(105 * scale);

        moduleForDesc = null;

        for (ModuleComponent moduleComponent : moduleComponents) {
            moduleComponent.setAlpha(getAlpha());
            moduleComponent.setX(getX());
            moduleComponent.setWidth(getWidth());
            moduleComponent.setY(getY() + offset);
            moduleComponent.setHeight(moduleComponent.getBaseHeight());

            if (MouseUtil.isHovered(mouseX, mouseY, moduleComponent.getX(), moduleComponent.getY(), moduleComponent.getWidth(), moduleComponent.getHeight())) {
                moduleForDesc = moduleComponent;
                lastModuleForDesc = moduleForDesc;
            }

            float expadedAnim = (float) moduleComponent.getExpandedAnim().getValue();

            if (expadedAnim > 0.0) {
                float componentOffset = 0;
                for (Component component : moduleComponent.getComponents()) {
                    float animComponent = (float) (component.getVisibleAnim().getValue());
                    component.setAlpha(animComponent * expadedAnim * moduleComponent.getAlpha());
                    if (animComponent > 0.0)
                        componentOffset += component.getHeight() * animComponent;
                }
                componentOffset *= expadedAnim;
                moduleComponent.setHeight(moduleComponent.getHeight() + componentOffset + gap * expadedAnim);
            }

            offset += moduleComponent.getHeight();
        }
        setHeight(offset);

        float fontSize = 8.5f * scale;

        float radius = header / 3f;

        RenderUtil.drawRound(getX(), getY(), getWidth(), getHeight(), new Vector4f(radius), ColorUtil.rgb(20, 20, 20, (int) (255 * getAlpha())));
        RenderUtil.drawRound(getX(), getY(), getWidth(), header, new Vector4f(radius, 0f, 0f, radius), ColorUtil.rgb(15, 15, 15, (int) (255 * getAlpha())));
        Fonts.bold.drawCenteredText(stack, category.name(), getX() + getWidth() / 2f, getY() + header / 2f - fontSize / 2f, ColorUtil.rgb(255, 255, 255, (int) (255 * getAlpha())), fontSize);

        for (ModuleComponent moduleComponent : moduleComponents) {
            moduleComponent.render(stack, mouseX, mouseY);
        }
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        super.mouseClick(mouseX, mouseY, mouse);

        getModuleComponents().forEach(moduleComponent -> moduleComponent.mouseClick(mouseX, mouseY, mouse));
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        super.mouseRelease(mouseX, mouseY, mouse);

        getModuleComponents().forEach(moduleComponent -> moduleComponent.mouseRelease(mouseX, mouseY, mouse));
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        super.keyPressed(key, scanCode, modifiers);

        getModuleComponents().forEach(moduleComponent -> moduleComponent.keyPressed(key, scanCode, modifiers));
    }

    public void drawDesc(MatrixStack matrixStack, float mouseX, float mouseY) {
        descAnimation.run(moduleForDesc == null || moduleForDesc != lastModuleForDesc || getAlpha() < 1f ? 0.0 : 1.0);
        descAnimation.setEasing(moduleForDesc == null || moduleForDesc != lastModuleForDesc || getAlpha() < 1f ? Easing.BACK_IN : Easing.BACK_OUT);

        if (descAnimation.getValue() > 0.0 && lastModuleForDesc != null) {
            float scale = UpdateFunctions.getInstance().getScaleFactor();
            float gap = 3f * scale;
            String desc = lastModuleForDesc.getModule().getDesc();
            float animation = (float) (descAnimation.getValue() * getAlpha());

            float size = 8f * scale;
            float maxWidth = 0f;
            float height = size + gap * 2f;
            List<String> lines = new ArrayList<>();

            for (String s : TextUtil.splitText(desc, 120f * scale, size)) {
                float lineWidth = Fonts.inter.getWidth(s, size);
                maxWidth = Math.max(maxWidth, lineWidth);
                height += size;
                lines.add(s);
            }

            float x = mouseX + gap * 2f;
            float y = mouseY - height + size;
            float textY = y + gap;

            maxWidth = Math.min(maxWidth, 120f * scale);

            RenderUtil.scaleStart(x, y + height, (float) descAnimation.getValue());

            RenderUtil.drawRound(x, y, maxWidth + gap * 2f, height - size, 2f * scale, ColorUtil.rgb(11, 11, 11, (int) (190 * animation)));

            for (String line : lines) {
                Fonts.inter.drawText(matrixStack, line, x + gap, textY, ColorUtil.rgb(255, 255, 255, (int) MathHelper.clamp(255 * animation, 0f, 255f)), size);
                textY += size;
            }

            RenderUtil.scaleStop();
        }
    }
}
