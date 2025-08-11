package kz.haru.api.module.setting.settings;

import kz.haru.api.module.setting.Setting;
import lombok.Getter;

import java.util.*;
import java.util.function.Supplier;

@Getter
public class MultiModeSetting extends Setting<List<String>> {
    private final List<String> possibleValues = new ArrayList<>();
    private final Set<String> selectedValues = new HashSet<>();

    public MultiModeSetting(String name) {
        super(name);
        this.value = new ArrayList<>();
    }

    public MultiModeSetting addValues(String... values) {
        possibleValues.addAll(Arrays.asList(values));
        return this;
    }

    @Override
    public MultiModeSetting value(List<String> values) {
        this.value = new ArrayList<>(values);
        this.selectedValues.clear();
        this.selectedValues.addAll(values);
        return this;
    }

    public MultiModeSetting select(String... values) {
        selectedValues.addAll(Arrays.asList(values));
        this.value = new ArrayList<>(selectedValues);
        return this;
    }

    public MultiModeSetting select(String value) {
        selectedValues.add(value);
        this.value = new ArrayList<>(selectedValues);
        return this;
    }

    public MultiModeSetting deselect(String value) {
        selectedValues.remove(value);
        this.value = new ArrayList<>(selectedValues);
        return this;
    }

    public boolean is(String value) {
        return selectedValues.contains(value);
    }

    public void toggle(String value) {
        if (is(value)) {
            deselect(value);
        } else {
            select(value);
        }
    }

    @Override
    public MultiModeSetting setVisible(Supplier<Boolean> condition) {
        return (MultiModeSetting) super.setVisible(condition);
    }
}