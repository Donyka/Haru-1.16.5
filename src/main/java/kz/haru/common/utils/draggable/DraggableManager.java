package kz.haru.common.utils.draggable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import kz.haru.api.module.Module;
import kz.haru.client.Haru;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

public class DraggableManager {

    @Getter
    private static final LinkedHashMap<String, Draggable> draggables = new LinkedHashMap<>();

    private static final File file = new File(Haru.getClientConfigsPath(), "/draggable.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

    public static Draggable installDraggable(Module module, String name, float x, float y) {
        Draggable draggable = new Draggable(module, name, x, y);
        draggables.put(name, draggable);
        return draggable;
    }

    public static void saveDraggables() {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }

        if (file.toPath().getFileSystem().isOpen()) {
            try {
                Files.writeString(file.toPath(), gson.toJson(draggables));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            System.err.println("File system closed. Could not save drag data.");
        }
    }

    public static void loadDraggables() {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            return;
        }

        try {
            String json = Files.readString(file.toPath());
            Type type = new TypeToken<Map<String, Draggable>>() {}.getType();
            Map<String, Draggable> loadedDraggables = gson.fromJson(json, type);

            if (loadedDraggables != null) {
                loadedDraggables.forEach((name, loadedDrag) -> {
                    Draggable currentDrag = draggables.get(name);
                    if (currentDrag != null) {
                        currentDrag.setX(loadedDrag.getX());
                        currentDrag.setY(loadedDrag.getY());
                        draggables.put(name, currentDrag);
                    }
                });
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
