package com.skyblock21.gui.components;

import com.skyblock21.gui.Theme;
import com.skyblock21.gui.ThemeManager;
import com.skyblock21.util.ColorUtil;
import com.skyblock21.util.Render2DUtil;
import io.wispforest.owo.mixin.ui.access.ClickableWidgetAccessor;
import io.wispforest.owo.ui.component.ButtonComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.function.Consumer;

public class Button extends ButtonComponent {

    private Identifier icon;
    private final Color bgColor;
    private final Color textColor;

    public Button(Text message, Color bgColor, Color textColor, Consumer<ButtonComponent> onPress) {
        super(message, onPress);
        this.bgColor = bgColor;
        this.textColor = textColor;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        Theme theme = ThemeManager.getCurrentTheme();
        TextRenderer textRenderer = theme.getTextRenderer();

        if (active) {
            if (hovered) {
                Render2DUtil.drawRoundedBox(context, getX(), getY(), width, height, 2, getBgColor());
            } else {
                Render2DUtil.drawRoundedBox(context, getX(), getY(), width, height, 2, getBgColor());
            }
        } else {
            Render2DUtil.drawRoundedBox(context, getX(), getY(), width, height, 2, getBgColor());
        }

        int color = theme.text.getRGB();

        if (this.icon != null) {
            Render2DUtil.drawTexturedQuad(context, this.icon, (int) (this.getX() + this.width / 2f - 8), (int) (this.getY() + (this.height - 8) / 2f), 16, 16, getTextColor());
        }

        if (this.textShadow) {
            context.drawCenteredTextWithShadow(textRenderer, getText(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, ColorUtil.getIntFromColor(getTextColor()));
        } else {
            context.drawText(textRenderer, getText(), (int) (this.getX() + this.width / 2f - textRenderer.getWidth(getText()) / 2f), (int) (this.getY() + (this.height - 8) / 2f), ColorUtil.getIntFromColor(getTextColor()), false);
        }

        var tooltip = ((ClickableWidgetAccessor) this).owo$getTooltip();
        if (this.hovered && tooltip.getTooltip() != null)
            context.drawTooltip(textRenderer, tooltip.getTooltip().getLines(MinecraftClient.getInstance()), HoveredTooltipPositioner.INSTANCE, mouseX, mouseY);
    }

    protected Text getText() {
        return getMessage();
    }

    protected Color getBgColor() {
        return bgColor;
    }

    protected Color getTextColor() {
        return textColor;
    }

    public Button setIcon(Identifier icon) {
        this.icon = icon;
        return this;
    }
}
