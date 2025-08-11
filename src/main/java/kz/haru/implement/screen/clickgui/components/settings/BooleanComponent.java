package kz.haru.implement.screen.clickgui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import kz.haru.api.module.setting.settings.BooleanSetting;
import kz.haru.client.functions.UpdateFunctions;
import kz.haru.common.utils.math.Vector4i;
import kz.haru.implement.screen.clickgui.components.build.Component;
import kz.haru.common.utils.color.ColorUtil;
import kz.haru.common.utils.math.MouseUtil;
import kz.haru.common.utils.render.RenderUtil;
import kz.haru.common.utils.text.TextUtil;
import kz.haru.common.utils.text.fonts.Fonts;
import lombok.Setter;
import net.minecraft.util.math.vector.Vector4f;

import java.util.List;

public class BooleanComponent extends Component {
    private final BooleanSetting setting;
    @Setter
    private boolean binding;
    
    // Анимация для яркости фона
    private float alphaAnimation = 0f;

    public BooleanComponent(BooleanSetting setting) {
        this.setting = setting;
        setHeight(14f);
        this.alphaAnimation = setting.getValue() ? 250f : 100f;
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);

        float scale = UpdateFunctions.getInstance().getScaleFactor();
        float gap = 2f * scale;
        float checkSize = 18f * scale;

        List<String> nameLines = TextUtil.splitText(setting.getName(), getWidth() - checkSize, 7f * scale);
        float textY = getY();

        for (String line : nameLines) {
            Fonts.regular.drawText(stack, line, getX(), textY + gap, ColorUtil.rgb(255, 255, 255, (int) (255 * getAlpha())), 7f * scale);
            textY += Fonts.regular.getHeight(7f * scale) + 2f * scale;
        }

        setHeight(textY - getY() + gap * 2f);
        float popkaMinus = scale;
        float checkHeight = checkSize / 2f;
        float popkaSize = checkHeight - popkaMinus;
        float checkY = getY() + getHeight() / 2f - checkHeight / 2f - gap / 2f;
        
        // Анимация яркости фона
        float targetAlpha = setting.getValue() ? 250f : 100f;
        alphaAnimation += (targetAlpha - alphaAnimation) * 0.1f;
        int alpha = (int) alphaAnimation;

        int alpha2 = (int) (alpha * getAlpha());
        Vector4i color = new Vector4i(ColorUtil.getClientColor(90, alpha2), ColorUtil.getClientColor(90, alpha2), ColorUtil.getClientColor(180, alpha2), ColorUtil.getClientColor(180, alpha2));



        RenderUtil.drawRound(getX() + getWidth() - checkSize, checkY, checkSize, checkHeight, new Vector4f(checkHeight / 2.5f,checkHeight / 2.5f,checkHeight / 2.5f,checkHeight / 2.5f),0.05f, color);
        RenderUtil.drawRound((float) (getX() + getWidth() - checkSize + (popkaSize + popkaMinus) * setting.getAnimation().getValue() + popkaMinus), checkY + popkaMinus, popkaSize - popkaMinus, checkHeight - popkaMinus * 2f, popkaSize / 2.5f, ColorUtil.rgb(255, 255, 255, (int) ((220 + 25 * setting.getAnimation().getValue()) * getAlpha())));
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        super.mouseClick(mouseX, mouseY, mouse);

        float scale = UpdateFunctions.getInstance().getScaleFactor();
        float gap = 2f * scale;
        float checkSize = 13f * scale - gap;

        if (MouseUtil.isHovered(mouseX, mouseY, getX(), getY() + getHeight() / 2f - checkSize / 2f, getWidth(), checkSize)) {
            if (mouse == 0) {
                setting.toggle();
            } else if (mouse == 2) {
                setBinding(!binding);
            }
        }

        if (binding && mouse != 0 && mouse != 1 && mouse != 2) {
            setting.setBind(mouse);
            setBinding(false);
        }
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        super.keyPressed(key, scanCode, modifiers);

        if (binding) {
            setting.setBind(key);
            setBinding(false);
        }
    }

    @Override
    public boolean isVisible() {
        return setting.isVisible();
    }
}