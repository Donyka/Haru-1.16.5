package kz.haru.api.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CommandRegister {
    String name();
    String desc() default "У данной комманды отсутствует вспомогательная информация";
}