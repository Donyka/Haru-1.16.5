package kz.haru.api.module.setting.settings;

import kz.haru.api.module.setting.Setting;
import lombok.Getter;

import java.util.function.Supplier;

@Getter
public class FloatSetting extends Setting<Float> {
    private float min = Float.MIN_VALUE;
    private float max = Float.MAX_VALUE;
    private float step = 1.0f;
    private boolean run = true;

    public FloatSetting(String name) {
        super(name);
    }

    @Override
    public FloatSetting value(Float value) {
        this.value = value;
        return this;
    }

    @Override
    public FloatSetting setVisible(Supplier<Boolean> condition) {
        return (FloatSetting) super.setVisible(condition);
    }

    public FloatSetting range(float min, float max) {
        this.min = min;
        this.max = max;
        return this;
    }

    public FloatSetting step(float step) {
        this.step = step;
        return this;
    }

    public FloatSetting runValue(boolean run) {
        this.run = run;
        return this;
    }
}