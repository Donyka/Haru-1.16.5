package kz.haru.api.module.setting.settings;

import kz.haru.api.module.setting.Setting;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@Getter
public class ModeSetting extends Setting<String> {
    private final List<String> possibleValues = new ArrayList<>();

    public ModeSetting(String name) {
        super(name);
    }

    public ModeSetting addValues(String... values) {
        possibleValues.addAll(Arrays.asList(values));
        if (value == null && !possibleValues.isEmpty()) {
            value = possibleValues.get(0);
        }
        return this;
    }

    @Override
    public ModeSetting value(String value) {
        this.value = value;
        return this;
    }

    @Override
    public ModeSetting setVisible(Supplier<Boolean> condition) {
        return (ModeSetting) super.setVisible(condition);
    }

    public boolean is(String value) {
        return this.value.equals(value);
    }
}