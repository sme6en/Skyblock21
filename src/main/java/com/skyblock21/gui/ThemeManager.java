package com.skyblock21.gui;

public class ThemeManager {
    private static Theme currentTheme = Theme.WHITE;

    public static Theme getCurrentTheme() {
        return currentTheme;
    }

    public static void setTheme(Theme theme) {
        currentTheme = theme;
    }
}