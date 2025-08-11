package kz.haru.implement.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import kz.haru.api.command.Command;
import kz.haru.api.command.CommandRegister;
import kz.haru.client.Haru;
import kz.haru.common.config.modules.ConfigModules;
import kz.haru.implement.commands.argument.AnyStringArgument;
import kz.haru.implement.commands.argument.ConfigNameArgument;
import net.minecraft.command.ISuggestionProvider;

import java.io.File;
import java.util.Arrays;

@CommandRegister(name = "config", desc = "Управление конфигурациями модулей")
public class ConfigCommand extends Command {

    @Override
    public void build(LiteralArgumentBuilder<ISuggestionProvider> builder) {
        builder.then(literal("load")
                        .then(argument("configName", new ConfigNameArgument())
                                .executes(context -> {
                                    String configName = context.getArgument("configName", String.class);
                                    ConfigModules.loadModules(configName);
                                    sendMessage("Конфигурация '" + configName + "' загружена.");
                                    return SINGLE_SUCCESS;
                                })))
                .then(literal("remove")
                        .then(argument("configName", new ConfigNameArgument())
                                .executes(context -> {
                                    String configName = context.getArgument("configName", String.class);
                                    File configFile = new File(Haru.getClientConfigsPath() + "/" + configName + ".json");
                                    if (configFile.delete()) {
                                        sendMessage("Конфигурация '" + configName + "' удалена.");
                                    } else {
                                        sendMessage("Конфигурация '" + configName + "' не найдена.");
                                    }
                                    return SINGLE_SUCCESS;
                                })))
                .then(literal("save")
                        .then(argument("configName", new AnyStringArgument())
                                .executes(context -> {
                                    String configName = context.getArgument("configName", String.class);
                                    ConfigModules.saveModules(configName);
                                    sendMessage("Конфигурация '" + configName + "' сохранена.");
                                    return SINGLE_SUCCESS;
                        })))
                .then(literal("list").executes(context -> {
                            File configFolder = new File(Haru.getClientModulesConfigPath());
                            String[] configFiles = configFolder.list((dir, name) -> name.endsWith(".json"));
                            if (configFiles != null && configFiles.length > 0) {
                                sendMessage("Доступные конфигурации:");
                                Arrays.stream(configFiles).forEach(file -> sendMessage("- " + file.replace(".json", "")));
                            } else {
                                sendMessage("Конфигурации не найдены.");
                            }
                            return SINGLE_SUCCESS;
                }))
                .then(literal("folder").executes(context -> {
                    try {
                        File configFolder = new File(Haru.getClientModulesConfigPath());

                        if (!configFolder.exists()) {
                            if (configFolder.mkdirs()) {
                                sendMessage("Папка конфигураций создана: " + configFolder.getAbsolutePath());
                            } else {
                                sendMessage("Не удалось создать папку конфигураций: " + configFolder.getAbsolutePath());
                                return SINGLE_SUCCESS;
                            }
                        }

                        if (!openFolderInExplorer(configFolder)) {
                            sendMessage("Не удалось открыть папку с конфигурациями.");
                        }
                    } catch (Exception e) {
                        sendMessage("Ошибка при открытии папки: " + e.getMessage());
                    }
                    return SINGLE_SUCCESS;
                }));
    }

    private boolean openFolderInExplorer(File folder) {
        String os = System.getProperty("os.name").toLowerCase();

        try {
            if (os.contains("win")) {
                String command = "explorer.exe /select," + folder.getAbsolutePath();
                Runtime.getRuntime().exec(command);
                return true;
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                String command = os.contains("mac") ? "open" : "xdg-open";
                Runtime.getRuntime().exec(new String[]{command, folder.getAbsolutePath()});
                return true;
            } else {
                java.awt.Desktop.getDesktop().open(folder);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
