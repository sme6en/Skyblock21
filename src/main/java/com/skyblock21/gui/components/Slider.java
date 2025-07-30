package com.skyblock21.gui.components;

import com.skyblock21.gui.Theme;
import com.skyblock21.gui.ThemeManager;
import com.skyblock21.util.ColorUtil;
import com.skyblock21.util.Render2DUtil;
import io.wispforest.owo.ui.component.DiscreteSliderComponent;
import io.wispforest.owo.ui.component.SliderComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

public class Slider extends DiscreteSliderComponent {

    public Slider(Sizing horizontalSizing, double min, double max) {
        super(horizontalSizing, min, max);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        Theme theme = ThemeManager.getCurrentTheme();

        int valueBarWidth = (int) (this.getWidth() * this.value);
        Render2DUtil.drawRoundedBox(context, this.getX(), this.getY(), this.getWidth(), this.getHeight(), theme.getRounding(), theme.getSecondaryBackground());
        if (valueBarWidth > 0) {
            Render2DUtil.drawRoundedBox(context, this.getX(), this.getY(), valueBarWidth, this.getHeight(), theme.getRounding(), theme.getPrimary());
        }

        this.drawScrollableText(context, theme.getTextRenderer(), 2, ColorUtil.getIntFromColor(theme.getBackground()));
    }
}
