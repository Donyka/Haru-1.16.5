package kz.haru.common.utils.text.fonts;

public class Fonts {
    public static Font
        icons,
        regular, bold, medium,inter;

    public static void init() {
        regular = new Font("regular");
        inter = new Font("inter");
        bold = new Font("bold");
        medium = new Font("medium");
        icons = new Font("icons");
    }
}