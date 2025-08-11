package kz.haru.implement.screen.clickgui.components;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import kz.haru.api.module.Module;
import kz.haru.api.module.setting.Setting;
import kz.haru.api.module.setting.settings.*;
import kz.haru.client.functions.KeyboardFunctions;
import kz.haru.client.functions.UpdateFunctions;
import kz.haru.common.utils.math.Vector4i;
import kz.haru.implement.modules.render.ClickGUIModule;
import kz.haru.implement.modules.render.InterfaceModule;
import kz.haru.implement.screen.clickgui.ClickGUIScreen;
import kz.haru.implement.screen.clickgui.components.build.Component;
import kz.haru.implement.screen.clickgui.components.settings.*;
import kz.haru.common.utils.animation.AnimationUtil;
import kz.haru.common.utils.animation.Easing;
import kz.haru.common.utils.color.ColorUtil;
import kz.haru.common.utils.math.MouseUtil;
import kz.haru.common.utils.render.RenderUtil;
import kz.haru.common.utils.render.StencilUtil;
import kz.haru.common.utils.text.fonts.Fonts;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.glfw.GLFW;

@Getter
@Setter
public class ModuleComponent extends Component {
    private final Module module;

    public float getBaseHeight() {
        return 18f * UpdateFunctions.getInstance().getScaleFactor();
    }
    private boolean last;
    private boolean expanded;
    private boolean bind;
    private AnimationUtil expandedAnim = new AnimationUtil(Easing.EXPO_OUT, 350);

    private final ObjectArrayList<Component> components = new ObjectArrayList<>();

    public ModuleComponent(Module module) {
        this.module = module;

        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof ModeSetting modeSetting) {
                components.add(new ModeComponent(modeSetting));
            }
            if (setting instanceof FloatSetting floatSetting) {
                components.add(new FloatComponent(floatSetting));
            }
            if (setting instanceof BooleanSetting booleanSetting) {
                components.add(new BooleanComponent(booleanSetting));
            }
            if (setting instanceof MultiModeSetting multiModeSetting) {
                components.add(new MultiModeComponent(multiModeSetting));
            }
            if (setting instanceof BindSetting bindSetting) {
                components.add(new BindComponent(bindSetting));
            }
            if (setting instanceof ColorSetting colorSetting) {
                components.add(new ColorComponent(colorSetting));
            }
        }
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);

        if (ClickGUIScreen.expandedModule != this && ClickGUIModule.onlyOneOpenedModule.getValue()) {
            expanded = false;
            expandedAnim.run(0.0);
        } else {
            expandedAnim.run(expanded ? 1.0 : 0.0);
        }

        float anim = (float) expandedAnim.getValue();
        float scale = UpdateFunctions.getInstance().getScaleFactor();
        float fontSize = 8f * scale;
        float gap = 5f * scale;
        float componentY = getY() + 20f * scale + gap * (1f - anim);
        int textColor = ColorUtil.interpolate(ColorUtil.rgb(255, 255, 255, (int) (255 * getAlpha())), ColorUtil.rgb(170, 170, 170, (int) (255 * getAlpha())), (float) module.getAnimation().getValue());
        String text = bind ? "Bind: " + KeyboardFunctions.getBind(module.getBind()) : module.getName();

        /*int rectColor = ClickGUIModule.enabledModuleBackground.getValue() ?
                ColorUtil.interpolate(ColorUtil.getClientColor(255 * getAlpha()), ColorUtil.rgb(31, 31, 31, (int) (255 * getAlpha())), (float) module.getAnimation().getValue())
                : ColorUtil.rgb(31, 31, 31, (int) (255 * getAlpha()));*/

        //Vector4i rectColor = new Vector4i(ColorUtil.rgb(12,123,123), ColorUtil.rgb(12,12,123), ColorUtil.rgb(12,123,12), ColorUtil.rgb(122,123,12));


        int rectColorNone = ColorUtil.rgb(31, 31, 31, (int) (255 * getAlpha()));
        int rectColor1 = ColorUtil.getClientColor((int) (getX() + getY()), (int) (255 * getAlpha()));
        int rectColor2 = ColorUtil.getClientColor((int) (getX() + getY() + 15), (int) (255 * getAlpha()));

        int q = (ColorUtil.interpolate(rectColorNone,rectColor1, (float) (1 - module.getAnimation().getValue())));
        int q2 = (ColorUtil.interpolate( rectColorNone,rectColor2, (float) (1 - module.getAnimation().getValue())));

        Vector4i rectColor = new Vector4i(
                q,
                q2,
                q,
                q2
        );
        
        
        
        
        float roundIfLast = isLast() ? (6f * scale) : 0f;

        if (anim > 0.0) {
            float yAnim = 15f * (1f - anim);
            StencilUtil.init();
            RenderUtil.drawRound(getX(), getY() + getBaseHeight() - scale, getWidth(), getHeight() - getBaseHeight() + scale * 2f, 0f, -1);
            StencilUtil.read(1);

            float componentGap = 1.2f;

            for (Component component : components) {
                component.getVisibleAnim().run(component.isVisible() ? 1.0 : 0.0);
                float animComponent = (float) component.getVisibleAnim().getValue();
                if (component.getVisibleAnim().getValue() > 0.0) {
                    component.setX(getX() + gap * componentGap);
                    component.setY(componentY - 5 * (1f - animComponent) - yAnim);
                    component.setWidth(getWidth() - gap * componentGap * 2f);
                    component.render(stack, mouseX, mouseY);
                    componentY += component.getHeight() * animComponent * anim;
                }
            }

            StencilUtil.unload();
        }

        RenderUtil.drawRound(getX(), getY(), getWidth(), getBaseHeight(), new Vector4f(0f, roundIfLast * (1f - anim), roundIfLast * (1f - anim), 0f),0.05f, rectColor);


        Fonts.medium.drawCenteredText(stack, text, getX() + getWidth() / 2f, getY() - fontSize / 2f + getBaseHeight() / 2f, textColor, fontSize);
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        super.keyPressed(key, scanCode, modifiers);

        if (bind) {
            if (key == GLFW.GLFW_KEY_DELETE) {
                module.setBind(-1);
            } else {
                module.setBind(key);
            }

            bind = false;
        }

        if (expandedAnim.getValue() < 0.9) return;
        for (Component component : components) {
            if (component.getVisibleAnim().getValue() < 0.9) continue;
            component.keyPressed(key, scanCode, modifiers);
        }
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        super.mouseClick(mouseX, mouseY, mouse);

        float scale = UpdateFunctions.getInstance().getScaleFactor();
        if (MouseUtil.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), getBaseHeight())) {
            if (mouse == 0) {
                module.toggle();
            } else if (mouse == 1 && !module.getSettings().isEmpty()) {
                expanded = !expanded;

                if (expanded) {
                    ClickGUIScreen.expandedModule = this;
                    expandedAnim.run(1.0);
                } else {
                    ClickGUIScreen.expandedModule = null;
                }
            } else if (mouse == 2) {
                bind = !bind;
            }
        }

        if (bind) {
            if (mouse != 0 && mouse != 1 && mouse != 2) {
                module.setBind(mouse);
                bind = false;
            }
        }

        if (expandedAnim.getValue() < 0.9) return;
        for (Component component : components) {
            if (component.getVisibleAnim().getValue() < 0.9) continue;
            component.mouseClick(mouseX, mouseY, mouse);
        }
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        super.mouseRelease(mouseX, mouseY, mouse);

        if (expandedAnim.getValue() < 0.9) return;
        for (Component component : components) {
            if (component.getVisibleAnim().getValue() < 0.9) continue;
            component.mouseRelease(mouseX, mouseY, mouse);
        }
    }
}