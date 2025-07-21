package com.skyblock21.util;

import java.awt.*;

public class ColorUtil {

    public enum Colors {
        WHITE,
        RED,
        GREEN,
        BLUE,
        YELLOW,
        CYAN,
        MAGENTA,
        PURPLE,
        LAVENDER,
        DARK_PURPLE,
        BLACK,
        GRAY,
        LIGHT_GRAY,
        PANEL,
    }

    public static int getColor(Colors color) {
        return switch(color) {
            case WHITE -> 0xFFFFFFFF;
            case RED -> 0xFFFF0000;
            case GREEN -> 0xFF00FF00;
            case BLUE -> 0xFF0000FF;
            case YELLOW -> 0xFFFFFF00;
            case CYAN -> 0xFF00FFFF;
            case MAGENTA -> 0xFFFF00FF;
            case PURPLE -> 0x9C27F5;
            case LAVENDER -> 0xE5B8FF;
            case DARK_PURPLE -> 0x7B1FA2;
            case BLACK -> 0xFF000000;
            case GRAY -> 0xFF212121;
            case LIGHT_GRAY -> 0xFFD3D3D3;
            case PANEL -> 0x60000000;
        };
    }

    public static int getIntFromColor(Color color) {
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        return (red << 16) | (green << 8) | blue;
    }

    public static int applyAlpha(int color, int alpha) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        return (alpha << 24) | (r << 16) | (g << 8) | b;
    }

    public static int interpolateColor(int color1, int color2, float factor) {
        factor = Math.max(0.0f, Math.min(1.0f, factor));

        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int) (a1 + factor * (a2 - a1));
        int r = (int) (r1 + factor * (r2 - r1));
        int g = (int) (g1 + factor * (g2 - g1));
        int b = (int) (b1 + factor * (b2 - b1));

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int getRainbowColor(float time, float saturation, float brightness) {
        float hue = (time % 1.0f);
        Color color = Color.getHSBColor(hue, saturation, brightness);
        return getIntFromColor(color);
    }

    public static Color getColorFromInt(int color) {
        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        return new Color(red, green, blue, alpha);
    }

}
