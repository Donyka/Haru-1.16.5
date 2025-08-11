package kz.haru.common.utils.text;

import kz.haru.common.utils.color.ColorUtil;
import kz.haru.common.utils.text.fonts.Fonts;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TextUtil {
    public static int getColorFromCode(char code, int alpha) {
        return switch (Character.toLowerCase(code)) {
            case '0' -> ColorUtil.setAlpha(0x000000, alpha);
            case '1' -> ColorUtil.setAlpha(0x0000AA, alpha);
            case '2' -> ColorUtil.setAlpha(0x00AA00, alpha);
            case '3' -> ColorUtil.setAlpha(0x00AAAA, alpha);
            case '4' -> ColorUtil.setAlpha(0xAA0000, alpha);
            case '5' -> ColorUtil.setAlpha(0xAA00AA, alpha);
            case '6' -> ColorUtil.setAlpha(0xFFAA00, alpha);
            case '7' -> ColorUtil.setAlpha(0xAAAAAA, alpha);
            case '8' -> ColorUtil.setAlpha(0x555555, alpha);
            case '9' -> ColorUtil.setAlpha(0x5555FF, alpha);
            case 'a' -> ColorUtil.setAlpha(0x55FF55, alpha);
            case 'b' -> ColorUtil.setAlpha(0x55FFFF, alpha);
            case 'c' -> ColorUtil.setAlpha(0xFF5555, alpha);
            case 'd' -> ColorUtil.setAlpha(0xFF55FF, alpha);
            case 'e' -> ColorUtil.setAlpha(0xFFFF55, alpha);
            case 'f' -> ColorUtil.setAlpha(0xFFFFFF, alpha);
            default -> ColorUtil.setAlpha(Color.WHITE.getRGB(), alpha);
        };
    }

    public static List<String> splitText(String text, float maxWidth, float fontSize) {
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        String[] words = text.split(" ");

        for (String word : words) {
            if (Fonts.inter.getWidth(currentLine + " " + word, fontSize) > maxWidth && !currentLine.isEmpty()) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder();
            }
            if (!currentLine.isEmpty()) {
                currentLine.append(" ");
            }
            currentLine.append(word);
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }
        return lines;
    }
}
