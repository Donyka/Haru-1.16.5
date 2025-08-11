package kz.haru.api.module.setting.settings;

import kz.haru.api.module.setting.Setting;

import java.util.function.Supplier;

public class BindSetting extends Setting<Integer> {
    public BindSetting(String name) {
        super(name);
    }

    @Override
    public BindSetting value(Integer value) {
        this.value = value;
        return this;
    }

    @Override
    public BindSetting setVisible(Supplier<Boolean> condition) {
        return (BindSetting) super.setVisible(condition);
    }
}
