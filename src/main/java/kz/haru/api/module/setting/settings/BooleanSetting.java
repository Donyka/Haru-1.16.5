package kz.haru.api.module.setting.settings;

import kz.haru.api.module.setting.Setting;
import kz.haru.common.utils.animation.AnimationUtil;
import kz.haru.common.utils.animation.Easing;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.glfw.GLFW;

import java.util.function.Supplier;

@Getter
public class BooleanSetting extends Setting<Boolean> {
    private final AnimationUtil animation = new AnimationUtil(Easing.EXPO_OUT, 500);
    @Setter private int bind = GLFW.GLFW_KEY_UNKNOWN;

    public BooleanSetting(String name) {
        super(name);
    }

    @Override
    public BooleanSetting value(Boolean value) {
        this.value = value;
        return this;
    }

    @Override
    public BooleanSetting setVisible(Supplier<Boolean> condition) {
        return (BooleanSetting) super.setVisible(condition);
    }

    public void toggle() {
        this.value = !this.value;
        animation.run(this.value ? 1.0 : 0.0);
    }

    public boolean hasBind() {
        return bind != GLFW.GLFW_KEY_UNKNOWN;
    }
}