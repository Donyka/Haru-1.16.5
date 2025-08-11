package kz.haru.common.config.clickgui;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import kz.haru.client.Haru;
import kz.haru.implement.screen.clickgui.Panel;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ConfigClickGui {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "panels.json";

    public static void savePanelPositions() {
        File configFile = new File(Haru.getClientConfigsPath(), CONFIG_FILE);
        Map<String, PanelPosition> positions = new HashMap<>();

        for (Panel panel : Haru.getInstance().getClickGuiScreen().panels) {
            String category = panel.getCategory().name();
            positions.put(category, new PanelPosition(panel.getX(), panel.getY()));
        }

        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(positions, writer);
        } catch (IOException e) {
            Haru.sendMessage("Ошибка сохранения позиций: " + e.getMessage());
        }
    }

    public static void loadPanelPositions() {
        File configFile = new File(Haru.getClientConfigsPath(), CONFIG_FILE);
        if (!configFile.exists()) return;

        try (FileReader reader = new FileReader(configFile)) {
            Type type = new TypeToken<Map<String, PanelPosition>>() {}.getType();
            Map<String, PanelPosition> positions = GSON.fromJson(reader, type);

            for (Panel panel : Haru.getInstance().getClickGuiScreen().panels) {
                PanelPosition pos = positions.get(panel.getCategory().name());
                if (pos != null) {
                    Haru.getInstance().getClickGuiScreen().yPanel = pos.y;
                    panel.setX(pos.x);
                    panel.setY(Haru.getInstance().getClickGuiScreen().yPanel);
                }
            }
        } catch (Exception e) {
            Haru.sendMessage("Ошибка загрузки позиций: " + e.getMessage());
        }
    }

    private static class PanelPosition {
        float x;
        float y;

        PanelPosition(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}