package kz.haru.client.functions;

import kz.haru.client.Haru;
import kz.haru.common.interfaces.IMinecraft;
import kz.haru.implement.modules.render.ClickGUIModule;
import kz.haru.implement.screen.clickgui.ClickGUIScreen;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class KeyboardFunctions implements IMinecraft {
    private static final List<Integer> mouseList = new ArrayList<>();

    public static void onKeyPress(int keyCode) {
        if (keyCode == -1) return;

        if (keyCode < 10) {
            mouseList.add(keyCode);
        }

        if (mc.currentScreen instanceof ClickGUIScreen && ClickGUIScreen.isExit) {
            if (keyCode == ClickGUIModule.get().getBind()) {
                ClickGUIScreen.isExit = false;
                kz.haru.implement.modules.render.ClickGUIModule.get().setEnabled(true);
            }
        }

        if (mc.currentScreen == null) {
            Haru.getInstance().getModuleManager().getModules().forEach(m -> {
                if (m.getBind() == keyCode && m.hasBind()) {
                    m.toggle();
                }
            });
            
            Haru.getInstance().getMacroManager().onKeyPressed(keyCode);
        }
    }

    public static boolean isPressed(int keyCode) {
        if (keyCode == -1) return false;

        if (keyCode < 10) {
            return GLFW.glfwGetMouseButton(window.getHandle(), keyCode) == GLFW.GLFW_PRESS;
        }

        return GLFW.glfwGetKey(window.getHandle(), keyCode) == GLFW.GLFW_PRESS;
    }

    public static int getBind(String s) {
        if (s.startsWith("MOUSE")) {
            try {
                int mouseButton = Integer.parseInt(s.substring(5));
                return switch (mouseButton) {
                    case 2 -> GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
                    case 3 -> GLFW.GLFW_MOUSE_BUTTON_4;
                    case 4 -> GLFW.GLFW_MOUSE_BUTTON_5;
                    default -> -1;
                };
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        if (s.equals("None")) {
            return -1;
        }

        try {
            for (Field field : GLFW.class.getDeclaredFields()) {
                if (field.getName().startsWith("GLFW_KEY_")) {
                    String fieldName = field.getName().substring("GLFW_KEY_".length());
                    if (convertGLFWPrefix(fieldName).equals(s)) {
                        return field.getInt(null);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static String getBind(int key) {
        if (key == -1) {
            return "None";
        }

        return switch (key) {
            case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> "MOUSE3";
            case GLFW.GLFW_MOUSE_BUTTON_4 -> "MOUSE4";
            case GLFW.GLFW_MOUSE_BUTTON_5 -> "MOUSE5";
            default -> {
                try {
                    for (Field field : GLFW.class.getDeclaredFields()) {
                        if (field.getName().startsWith("GLFW_KEY_")) {
                            if (field.getInt(null) == key) {
                                String fieldName = field.getName().substring("GLFW_KEY_".length());
                                yield convertGLFWPrefix(fieldName);
                            }
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                yield "None";
            }
        };
    }

    public static List<String> getKeyNames() {
        List<String> keyNames = new ArrayList<>();

        try {
            for (Field field : GLFW.class.getDeclaredFields()) {
                if (field.getName().startsWith("GLFW_KEY_")) {
                    String fieldName = field.getName().substring("GLFW_KEY_".length());
                    keyNames.add(convertGLFWPrefix(fieldName));
                }
            }
        } catch (Exception ignored) {
        }

        keyNames.add("MOUSE3");
        keyNames.add("MOUSE4");
        keyNames.add("MOUSE5");

        return keyNames;
    }

    private static String convertGLFWPrefix(String input) {
        return switch (input) {
            case "PRINT_SCREEN" -> "PrintScreen";
            case "CAPS_LOCK" -> "CapsLock";
            case "LEFT_ALT" -> "LAlt";
            case "RIGHT_ALT" -> "RAlt";
            case "LEFT_CONTROL" -> "LCtrl";
            case "RIGHT_CONTROL" -> "RCtrl";
            case "LEFT_SHIFT" -> "LShift";
            case "RIGHT_SHIFT" -> "RShift";
            case "LEFT_SUPER" -> "LSuper";
            case "RIGHT_SUPER" -> "RSuper";
            case "LEFT_BRACKET" -> "LBracket";
            case "RIGHT_BRACKET" -> "RBracket";
            case "SEMICOLON" -> "Semicolon";
            case "APOSTROPHE" -> "Apostrophe";
            case "COMMA" -> "Comma";
            case "PERIOD" -> "Period";
            case "SLASH" -> "Slash";
            case "BACKSLASH" -> "Backslash";
            case "EQUAL" -> "Equals";
            case "MINUS" -> "Minus";
            case "GRAVE_ACCENT" -> "Grave";
            case "SPACE" -> "Space";
            case "ENTER" -> "Enter";
            case "ESCAPE" -> "Escape";
            case "TAB" -> "Tab";
            case "BACKSPACE" -> "Backspace";
            case "INSERT" -> "Insert";
            case "DELETE" -> "Delete";
            case "HOME" -> "Home";
            case "END" -> "End";
            case "PAGE_UP" -> "PageUp";
            case "PAGE_DOWN" -> "PageDown";
            case "UP" -> "Up";
            case "DOWN" -> "Down";
            case "LEFT" -> "Left";
            case "RIGHT" -> "Right";
            case "NUM_LOCK" -> "NumLock";
            case "KP_0" -> "Numpad0";
            case "KP_1" -> "Numpad1";
            case "KP_2" -> "Numpad2";
            case "KP_3" -> "Numpad3";
            case "KP_4" -> "Numpad4";
            case "KP_5" -> "Numpad5";
            case "KP_6" -> "Numpad6";
            case "KP_7" -> "Numpad7";
            case "KP_8" -> "Numpad8";
            case "KP_9" -> "Numpad9";
            case "KP_DECIMAL" -> "NumpadDecimal";
            case "KP_DIVIDE" -> "NumpadDivide";
            case "KP_MULTIPLY" -> "NumpadMultiply";
            case "KP_SUBTRACT" -> "NumpadSubtract";
            case "KP_ADD" -> "NumpadAdd";
            case "KP_ENTER" -> "NumpadEnter";
            case "KP_EQUAL" -> "NumpadEquals";
            default -> input.replace("_", "").substring(0, 1).toUpperCase() + input.replace("_", "").substring(1).toLowerCase();
        };
    }
}