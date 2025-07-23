package com.skyblock21.gui;

import com.skyblock21.util.ColorUtil;
import me.x150.renderer.fontng.FTLibrary;
import me.x150.renderer.fontng.Font;
import me.x150.renderer.fontng.FontScalingRegistry;
import net.minecraft.client.font.TextRenderer;

public enum Theme {
    WHITE(
            0x1d53bc, // primary
            0x5ba7f8, // secondary
            0xFFFFFFFF, // background
            0xd5ced0, // secondaryBackground
            0xf1f5f9,
            0x1d2026,
            "roboto"
    ),
    DARK(
            0x5ba7f8, // primary
            0x1d53bc, // secondary
            0x1d2026, // background
            0x17191e, // secondaryBackground
            0x64748b,
            0xFFFFFFFF, // text
            "roboto"
    );

    public final int primary;
    public final int secondary;
    public final int background;
    public final int secondaryBackground;
    public final int border;
    public final int text;
    public final String font;

    Theme(int primary, int secondary, int background, int secondaryBackground, int border, int text, String themeFont) {
        this.primary = primary;
        this.secondary = secondary;
        this.background = background;
        this.secondaryBackground = secondaryBackground;
        this.border = border;
        this.text = text;
        this.font = themeFont;
    }

    public int getPrimaryHover() {
        return ColorUtil.lighten(primary, 0.1f);
    }

    public int getPrimaryActive() {
        return ColorUtil.darken(primary, 0.1f);
    }

    public int getButtonHover() {
        return ColorUtil.darken(secondaryBackground, 0.1f);
    }

    public int getButtonActive() {
        return ColorUtil.lighten(secondaryBackground, 0.1f);
    }

    public int getTextSecondary() {
        return ColorUtil.applyAlpha(text, 0.7f);
    }

    public int getBorder() {
        return this.border;
    }

    public int getBorderHover() {
        return ColorUtil.lighten(getBorder(), 0.2f);
    }

    public String getFont() {
        return this.font;
    }

    public TextRenderer getTextRenderer() {
        return FontManager.getFont(this.font, 11);
    }
}
