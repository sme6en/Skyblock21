package com.skyblock21.gui.components;

import com.skyblock21.gui.FontManager;
import com.skyblock21.gui.Theme;
import com.skyblock21.gui.ThemeManager;
import io.wispforest.owo.mixin.ui.access.ClickableWidgetAccessor;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.component.Components;
import me.x150.renderer.fontng.Font;
import me.x150.renderer.fontng.GlyphBuffer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.text.Text;

import java.util.function.Consumer;

import static net.minecraft.text.Text.literal;

public class Button extends ButtonComponent {

    private static final TextRenderer textRenderer = FontManager.getFont(ThemeManager.getCurrentTheme().getFont(), 11);

    public Button(Text message, Consumer<ButtonComponent> onPress) {
        super(message, onPress);
//        ThemeManager.setTheme(Theme.DARK);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        Theme theme = ThemeManager.getCurrentTheme();

//        this.renderer.draw((OwoUIDrawContext) context, this, delta);
        if (active) {
            if (hovered) {
                context.fill(getX(), getY(), getX() + width, getY() + height, theme.getButtonHover());
            } else {
                context.fill(getX(), getY(), getX() + width, getY() + height, theme.getButtonActive());
            }
        } else {
            context.fill(getX(), getY(), getX() + width, getY() + height, theme.secondaryBackground);
        }

//        var textRenderer = MinecraftClient.getInstance().textRenderer;
        int color = theme.text;

        if (this.textShadow) {
            context.drawCenteredTextWithShadow(textRenderer, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, color);
        } else {
            context.drawText(textRenderer, this.getMessage(), (int) (this.getX() + this.width / 2f - textRenderer.getWidth(this.getMessage()) / 2f), (int) (this.getY() + (this.height - 8) / 2f), color, false);
        }

        var tooltip = ((ClickableWidgetAccessor) this).owo$getTooltip();
        if (this.hovered && tooltip.getTooltip() != null)
            context.drawTooltip(textRenderer, tooltip.getTooltip().getLines(MinecraftClient.getInstance()), HoveredTooltipPositioner.INSTANCE, mouseX, mouseY);

        VertexConsumerProvider.Immediate im = VertexConsumerProvider.immediate(new BufferAllocator(1536));
    }

}
