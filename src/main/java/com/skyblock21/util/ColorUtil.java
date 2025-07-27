package com.skyblock21.util;

import java.awt.*;

public class ColorUtil {

    public static int getAlpha(int color) {
        return (color >>> 24) & 0xFF;
    }

    public static int getRed(int color) {
        return (color >>> 16) & 0xFF;
    }

    public static int getGreen(int color) {
        return (color >>> 8) & 0xFF;
    }

    public static int getBlue(int color) {
        return color & 0xFF;
    }

    public static int argb(int alpha, int red, int green, int blue) {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    public static int rgb(int red, int green, int blue) {
        return argb(255, red, green, blue);
    }

    public static int getIntFromColor(Color color) {
        int Alpha = ((color.getAlpha()) << 24) & 0xFF000000;
        int R = ((color.getRed()) << 16) & 0x00FF0000;
        int G = ((color.getGreen()) << 8) & 0x0000FF00;
        int B = (color.getBlue()) & 0x000000FF;

        return Alpha | R | G | B;
    }

    public static int applyAlpha(int color, int alpha) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        return (alpha << 24) | (r << 16) | (g << 8) | b;
    }

    public static int applyAlpha(int color, float alpha) {
        int alphaInt = Math.max(0, Math.min(255, (int) (alpha * 255)));
        return (color & 0x00FFFFFF) | (alphaInt << 24);
    }

    public static Color applyAlpha(Color color, int alpha) {
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        return new Color(red, green, blue, alpha);
    }

    public static Color applyAlpha(Color color, float alpha) {
        int alphaInt = Math.max(0, Math.min(255, (int) (alpha * 255)));
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        return new Color(red, green, blue, alphaInt);
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

    public static int lighten(int color, float factor) {
        int alpha = getAlpha(color);
        int red = getRed(color);
        int green = getGreen(color);
        int blue = getBlue(color);

        red = Math.min(255, (int) (red + (255 - red) * factor));
        green = Math.min(255, (int) (green + (255 - green) * factor));
        blue = Math.min(255, (int) (blue + (255 - blue) * factor));

        return argb(alpha, red, green, blue);
    }

    public static int darken(int color, float factor) {
        int alpha = getAlpha(color);
        int red = getRed(color);
        int green = getGreen(color);
        int blue = getBlue(color);

        red = Math.max(0, (int) (red * (1.0f - factor)));
        green = Math.max(0, (int) (green * (1.0f - factor)));
        blue = Math.max(0, (int) (blue * (1.0f - factor)));

        return argb(alpha, red, green, blue);
    }

    public static Color lighten(Color color, float factor) {
        int alpha = color.getAlpha();
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        red = Math.min(255, (int) (red + (255 - red) * factor));
        green = Math.min(255, (int) (green + (255 - green) * factor));
        blue = Math.min(255, (int) (blue + (255 - blue) * factor));

        return new Color(red, green, blue, alpha);
    }

    public static Color darken(Color color, float factor) {
        int alpha = color.getAlpha();
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        red = Math.max(0, (int) (red * (1.0f - factor)));
        green = Math.max(0, (int) (green * (1.0f - factor)));
        blue = Math.max(0, (int) (blue * (1.0f - factor)));

        return new Color(red, green, blue, alpha);
    }

}
