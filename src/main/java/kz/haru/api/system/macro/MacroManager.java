package kz.haru.api.system.macro;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.Value;
import kz.haru.common.interfaces.IMinecraft;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class MacroManager implements IMinecraft {
    public List<Macro> macroList = new ArrayList<>();
    private final File macroFile = new File(System.getProperty("user.dir"), "saves/New World/DIM1/data/saves/misc/macro.json");

    public void init() {
        try {
            if (!macroFile.exists()) {
                if (!macroFile.getParentFile().exists()) {
                    macroFile.getParentFile().mkdirs();
                }
                macroFile.createNewFile();
            } else {
                readFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isEmpty() {
        return macroList.isEmpty();
    }

    public void addMacro(String name, String message, int key) {
        macroList.add(new Macro(name, message, key));
        writeFile();
    }

    public boolean hasMacro(String macroName) {
        for (Macro macro : macroList) {
            if (macro.getName().equalsIgnoreCase(macroName)) {
                return true;
            }
        }
        return false;
    }

    public void deleteMacro(String name) {
        if (macroList.stream()
                .anyMatch(macro -> macro.getName().equals(name))) {
            macroList.removeIf(macro -> macro.getName().equalsIgnoreCase(name));
            writeFile();
        }
    }

    public void clearList() {
        if (!macroList.isEmpty()) {
            macroList.clear();
        }
        writeFile();
    }

    public void onKeyPressed(int key) {
        if (mc.player == null) {
            return;
        }

        macroList.stream()
                .filter(macro -> macro.getKey() == key)
                .findFirst()
                .ifPresent(macro -> {
                    try {
                        mc.player.sendChatMessage(macro.getMessage());
                    } catch (Exception e) {
                        sendMessage("Error sending command: " + e.getMessage());
                    }
                });
    }

    @SneakyThrows
    public void writeFile() {
        StringBuilder builder = new StringBuilder();
        macroList.forEach(macro -> builder.append(macro.getName())
                .append(":").append(macro.getMessage())
                .append(":").append(macro.getKey())
                .append("\n"));
        Files.write(macroFile.toPath(), builder.toString().getBytes());
    }

    @SneakyThrows
    private void readFile() {
        if (macroFile.exists() && macroFile.length() > 0) {
            FileInputStream fileInputStream = new FileInputStream(macroFile.getAbsolutePath());
            @Cleanup
            BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(fileInputStream)));
            String line;
            while ((line = reader.readLine()) != null) {
                String curLine = line.trim();
                String[] parts = curLine.split(":");
                if (parts.length >= 3) {
                    String name = parts[0];
                    String command = parts[1];
                    try {
                        int key = Integer.parseInt(parts[2]);
                        macroList.add(new Macro(name, command, key));
                    } catch (NumberFormatException e) {
                        sendMessage("Error parsing key code for macro: " + name);
                    }
                }
            }
        }
    }

    @Value
    public static class Macro {
        String name;
        String message;
        int key;
    }
}
