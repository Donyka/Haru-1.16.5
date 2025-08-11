package kz.haru.api.module;

import org.lwjgl.glfw.GLFW;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleRegister {
    String name();
    Category category() default Category.MISC;
    int bind() default GLFW.GLFW_KEY_UNKNOWN;
    String desc() default "No description";
}