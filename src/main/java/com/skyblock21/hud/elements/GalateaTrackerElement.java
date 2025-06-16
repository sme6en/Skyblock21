package com.skyblock21.hud.elements;

import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.features.foraging.GalateaTracker;
import com.skyblock21.hud.HudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public class GalateaTrackerElement extends HudElement {

    public GalateaTrackerElement(int x, int y) {
        super(x, y);
    }

    @Override
    protected void renderElement(DrawContext context) {
        String content = GalateaTracker.getHudText();
        if (content.isEmpty()) return;

        MatrixStack matrices = context.getMatrices();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        // 20% bigger
        matrices.push();
        matrices.scale(1.2f, 1.2f, 1.0f);
        context.drawTextWithShadow(textRenderer, "§lGalatea Tracker", 2, 2, Color.GREEN.getRGB());
        matrices.pop();
        String[] lines = content.split("\n");
        for (int i = 0; i < lines.length; i++) {
            context.drawTextWithShadow(textRenderer, lines[i], 8, 4 + (textRenderer.fontHeight + VERTICAL_PADDING) * (i + 1), Color.WHITE.getRGB());
        }
    }

    @Override
    protected void renderDummy(DrawContext context) {
        String dummyText = GalateaTracker.getDummyHudText();
        MatrixStack matrices = context.getMatrices();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        // 20% bigger
        matrices.push();
        matrices.scale(1.2f, 1.2f, 1.0f);
        context.drawTextWithShadow(textRenderer, "§lGalatea Tracker", 2, 2, Color.GREEN.getRGB());
        matrices.pop();

        String[] lines = dummyText.split("\n");
        for (int i = 0; i < lines.length; i++) {
            context.drawTextWithShadow(textRenderer, lines[i], 8, 4 + (textRenderer.fontHeight + VERTICAL_PADDING) * (i + 1), Color.WHITE.getRGB());
        }
    }

    @Override
    public int getWidth() {
        return (int) ((MinecraftClient.getInstance().textRenderer.getWidth("§lGalatea Tracker") * 1.2 + HORIZONTAL_PADDING) * 1.3);
    }

    @Override
    public int getHeight() {
        int lineCount = 7;
        return (MinecraftClient.getInstance().textRenderer.fontHeight) * lineCount + VERTICAL_PADDING * (lineCount - 1);
    }

    @Override
    public boolean shouldRenderDummy() {
        return super.shouldRenderDummy() || GalateaTracker.getHudText().isEmpty();
    }

    @Override
    public boolean isEnabled() {
        return Skyblock21ConfigManager.get().foraging.galateaTracker;
    }
}
