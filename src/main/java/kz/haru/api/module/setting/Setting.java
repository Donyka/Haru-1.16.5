package kz.haru.api.module.setting;

import lombok.Getter;
import lombok.Setter;

import java.util.function.Supplier;

@Getter
@Setter
public abstract class Setting<T> implements ISetting {
    protected String name;
    protected T value;
    protected Supplier<Boolean> visibilityCondition = () -> true;

    public Setting(String name) {
        this.name = name;
    }

    @Override
    public Setting<T> setVisible(Supplier<Boolean> condition) {
        this.visibilityCondition = condition;
        return this;
    }

    public boolean isVisible() {
        return visibilityCondition.get();
    }

    public abstract Setting<T> value(T value);
}