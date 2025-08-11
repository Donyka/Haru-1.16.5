package kz.haru.api.event;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventTarget {
    Priority priority() default Priority.NORMAL;
}
