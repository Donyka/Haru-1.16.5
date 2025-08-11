package kz.haru.api.module;

import kz.haru.api.module.setting.Setting;
import kz.haru.client.Haru;
import kz.haru.common.interfaces.IMinecraft;
import kz.haru.common.utils.animation.AnimationUtil;
import kz.haru.common.utils.animation.Easing;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class Module implements IMinecraft {
    final String name;
    final String desc;
    final Category category;
    boolean enabled;
    @Setter int bind;

    AnimationUtil animation = new AnimationUtil(Easing.EXPO_OUT, 500);
    @Setter List<Setting<?>> settings = new ArrayList<>();

    public Module() {
        Class<?> clazz = this.getClass();
        ModuleRegister annotation = clazz.getAnnotation(ModuleRegister.class);
        if (annotation == null) {
            throw new RuntimeException("ModuleRegister annotation not found on " + clazz.getName());
        }
        this.name = annotation.name();
        this.desc = annotation.desc();
        this.category = annotation.category();
        this.bind = annotation.bind();
    }

    public static <T extends Module> T get(Class<T> moduleClass) {
        return Haru.getInstance().getModuleManager().get(moduleClass);
    }

    public void setup(Setting<?>... settings) {
        this.settings.addAll(Arrays.asList(settings));
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(Boolean newState) {
        if (newState == enabled) {
            return;
        }

        enabled = newState;

        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public boolean hasBind() {
        return bind != GLFW.GLFW_KEY_UNKNOWN;
    }

    public void onDisable() {
        Haru.getInstance().getEventManager().unregister(this);
    }

    public void onEnable() {
        Haru.getInstance().getEventManager().register(this);
    }
}
