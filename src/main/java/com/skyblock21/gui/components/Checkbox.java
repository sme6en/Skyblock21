package com.skyblock21.gui.components;

import com.skyblock21.gui.Theme;
import com.skyblock21.gui.ThemeManager;
import com.skyblock21.util.ColorUtil;
import com.skyblock21.util.Render2DUtil;
import io.wispforest.owo.ui.component.CheckboxComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

public class Checkbox extends CheckboxComponent {

    public Checkbox(Text message) {
        super(message);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        Theme theme = ThemeManager.getCurrentTheme();
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        TextRenderer textRenderer = minecraftClient.textRenderer;

        int i = getCheckboxSize(textRenderer);
        if (this.isChecked()) {
            Render2DUtil.drawRoundedBox(context, this.getX(), this.getY() + 2, getWidth() - theme.getTextRenderer().getWidth(getMessage()) - (getMessage().getString().isEmpty() ? 0 : 8), getHeight(), 2,  theme.primary);
        } else {
            Render2DUtil.drawOutlinedRoundedBox(context, this.getX(), this.getY() + 2, getWidth() - theme.getTextRenderer().getWidth(getMessage()) - (getMessage().getString().isEmpty() ? 0 : 8), getHeight(), 2, theme.primary, theme.getSecondaryBackground());
        }
        int j = 1 + this.getX() + i + 4;
        int k = this.getY() + i / 2 - theme.getTextRenderer().fontHeight / 2;
//        this.textWidget.setPosition(j, k);
        context.drawText(theme.getTextRenderer(), getMessage(), j, k+ 2, ColorUtil.getIntFromColor(theme.text), false);
//        this.textWidget.renderWidget(context, mouseX, mouseY, deltaTicks);
    }
}
