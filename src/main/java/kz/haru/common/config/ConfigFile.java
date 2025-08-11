package kz.haru.common.config;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public final class ConfigFile {
    private static final GsonBuilder gson = new GsonBuilder().setPrettyPrinting();

    public static void loadStrings(File file, Set<String> strings) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                System.out.println("Error creating config file: " + e.getMessage());
                return;
            }
        }

        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<HashSet<String>>() {}.getType();
            Set<String> loadedStrings = gson.create().fromJson(reader, type);
            strings.clear();
            if (loadedStrings != null) {
                strings.addAll(loadedStrings);
            }
        } catch (Exception e) {
            System.out.println("Error loading config: " + e.getMessage());
        }
    }

    public static void saveStrings(File file, Set<String> strings) {
        File parentDir = file.getParentFile();
        if (parentDir != null) {
            parentDir.mkdirs();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                System.out.println("Error creating config file during save: " + e.getMessage());
                return;
            }
        }

        try (FileWriter writer = new FileWriter(file)) {
            gson.create().toJson(strings, writer);
        } catch (Exception e) {
            System.out.println("Error saving config: " + e.getMessage());
        }
    }
}
