package com.skyblock21.gui;

import com.skyblock21.util.ColorUtil;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;

import java.awt.*;

public enum Theme {
    LIGHT(
            new Color(20, 83, 188, 255), // primary
            new Color(91, 167, 248, 255), // secondary
            new Color(255,255,255,255), // background
            new Color(213, 206, 208, 255), // secondaryBackground
            new Color(241, 245, 249, 255),
            new Color(29,32,38, 255),
            "roboto",
            12f
    ),
    DARK(
            new Color(91, 167, 248, 255), // primary
            new Color(20, 83, 188, 255), // secondary
            new Color(29,32,38, 255), // background
            new Color(23, 25,30, 255), // secondaryBackground
            new Color(100, 116, 139, 255),
            new Color(255,255,255,255), // text
            "roboto",
            12f
    ),
    CANDY(
            new Color(255, 20, 147, 255),
            new Color(255, 105, 180, 255),
            new Color(255, 248, 220, 255),
            new Color(255, 228, 225, 255),
            new Color(255, 182, 193, 255),
            new Color(139, 69, 19, 255),
            "roboto",
            12f
    );

    @Getter
    public final Color primary;
    @Getter
    public final Color secondary;
    @Getter
    public final Color background;
    @Getter
    private final Color secondaryBackground;
    @Getter
    private final Color border;
    @Getter
    public final Color text;
    @Getter
    private final String font;

    @Getter
    private final float rounding;

    private TextRenderer textRenderer;

    Theme(Color primary, Color secondary, Color background, Color secondaryBackground, Color border, Color text, String themeFont, float rounding) {
        this.primary = primary;
        this.secondary = secondary;
        this.background = background;
        this.secondaryBackground = secondaryBackground;
        this.border = border;
        this.text = text;
        this.font = themeFont;
        this.textRenderer = FontManager.getFont(themeFont, 9);
        this.rounding = rounding;
    }

    public Color getPrimaryHover() {
        return ColorUtil.lighten(primary, 0.1f);
    }

    public Color getPrimaryActive() {
        return ColorUtil.darken(primary, 0.1f);
    }

    public Color getSecondaryHover() {
        return ColorUtil.lighten(secondary, 0.1f);
    }

    public Color getSecondaryActive() {
        return ColorUtil.darken(secondary, 0.1f);
    }

    public Color getButtonHover() {
        return ColorUtil.darken(secondaryBackground, 0.1f);
    }

    public Color getButtonActive() {
        return ColorUtil.lighten(secondaryBackground, 0.1f);
    }

    public Color getTextSecondary() {
        return ColorUtil.applyAlpha(text, 0.7f);
    }

    public Color getBorderHover() {
        return ColorUtil.lighten(getBorder(), 0.2f);
    }

    public TextRenderer getTextRenderer() {
        if (textRenderer == null ||  textRenderer == MinecraftClient.getInstance().textRenderer) {
            textRenderer = FontManager.getFont(font, 9);
        }

        return textRenderer;
    }
}
