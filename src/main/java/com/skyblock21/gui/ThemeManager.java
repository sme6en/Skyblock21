package com.skyblock21.gui;

import lombok.Getter;

public class ThemeManager {
    @Getter
    private static Theme currentTheme = Theme.LIGHT;

    public static void setTheme(Theme theme) {
        currentTheme = theme;
    }
}