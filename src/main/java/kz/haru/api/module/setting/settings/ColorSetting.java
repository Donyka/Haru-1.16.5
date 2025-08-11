package kz.haru.api.module.setting.settings;

import kz.haru.api.module.setting.Setting;

import java.util.function.Supplier;

public class ColorSetting extends Setting<Integer> {
    public ColorSetting(String name) {
        super(name);
    }

    @Override
    public ColorSetting value(Integer value) {
        this.value = value;
        return this;
    }

    @Override
    public ColorSetting setVisible(Supplier<Boolean> condition) {
        return (ColorSetting) super.setVisible(condition);
    }
}
