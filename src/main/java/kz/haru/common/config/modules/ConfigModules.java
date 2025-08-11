package kz.haru.common.config.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import kz.haru.api.module.Module;
import kz.haru.api.module.setting.Setting;
import kz.haru.api.module.setting.settings.*;
import kz.haru.client.Haru;
import kz.haru.implement.screen.clickgui.Panel;
import kz.haru.implement.screen.clickgui.components.ModuleComponent;
import kz.haru.implement.screen.clickgui.components.build.Component;
import kz.haru.implement.screen.clickgui.components.settings.FloatComponent;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigModules {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void saveModules(String configName) {
        File configFile = new File(Haru.getClientModulesConfigPath() + "/" + configName + ".json");
        Map<String, ModuleConfig> moduleConfigs = new HashMap<>();

        for (Module module : Haru.getInstance().getModuleManager().getModules()) {
            ModuleConfig config = new ModuleConfig();
            config.setEnabled(module.isEnabled());
            config.setBind(module.getBind());

            Map<String, Object> settingsMap = new HashMap<>();
            for (Setting<?> setting : module.getSettings()) {
                settingsMap.put(setting.getName(), setting.getValue());
            }
            config.setSettings(settingsMap);

            moduleConfigs.put(module.getName(), config);
        }

        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(moduleConfigs, writer);
        } catch (Exception e) {
            System.err.println("Failed to save modules config: " + e.getMessage());
        }
    }

    public static void loadModules(String configName) {
        File configFile = new File(Haru.getClientModulesConfigPath() + "/" + configName + ".json");
        if (!configFile.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            Type type = new TypeToken<Map<String, ModuleConfig>>() {}.getType();
            Map<String, ModuleConfig> moduleConfigs = GSON.fromJson(reader, type);

            for (Module module : Haru.getInstance().getModuleManager().getModules()) {
                ModuleConfig config = moduleConfigs.get(module.getName());
                if (config != null) {
                    if (!module.getName().equals("Free Camera") && !module.getName().equals("Click GUI")) {
                        module.setEnabled(config.isEnabled());
                    }
                    module.setBind(config.getBind());

                    for (Setting<?> setting : module.getSettings()) {
                        Object value = config.getSettings().get(setting.getName());
                        if (value != null) {
                            setSettingValue(setting, value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load modules config: " + e.getMessage());
        }
    }

    private static void setSettingValue(Setting<?> setting, Object value) {
        try {
            if (setting instanceof BooleanSetting booleanSetting) {
                booleanSetting.value((Boolean) value);
            } else if (setting instanceof FloatSetting floatSetting) {
                float newValue = ((Number) value).floatValue();
                floatSetting.value(newValue);

                for (Panel panel : Haru.getInstance().getClickGuiScreen().panels) {
                    for (ModuleComponent moduleComponent : panel.getModuleComponents()) {
                        for (Component component : moduleComponent.getComponents()) {
                            if (component instanceof FloatComponent floatComponent) {
                                if (floatComponent.setting == setting) {
                                    floatComponent.lastValue = newValue;
                                }
                            }
                        }
                    }
                }

            } else if (setting instanceof ModeSetting modeSetting) {
                modeSetting.value((String) value);
            } else if (setting instanceof MultiModeSetting multiModeSetting) {
                multiModeSetting.value((List<String>) value);
            } else if (setting instanceof BindSetting bindSetting) {
                bindSetting.value(((Number) value).intValue());
            } else if (setting instanceof ColorSetting colorSetting) {
                colorSetting.value(((Number) value).intValue());
            }
            else {
                throw new IllegalArgumentException("Unsupported setting type: " + setting.getClass().getSimpleName());
            }
        } catch (ClassCastException e) {
            System.err.println("Ошибочка! '" + setting.getName() + "': " + e.getMessage());
        }
    }

    @Setter
    @Getter
    private static class ModuleConfig {
        private boolean enabled;
        private int bind;
        private Map<String, Object> settings;

    }
}