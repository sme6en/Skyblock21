package com.skyblock21.hud.elements;

import com.skyblock21.hud.HudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class TestHudElement extends HudElement {

    public TestHudElement(int x, int y) {
        super(x, y);
    }

    @Override
    protected void renderElement(DrawContext context) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        context.drawTextWithShadow(textRenderer, "Test Element", 2, 2, 0xFFFFFF);
    }

    @Override
    protected void renderDummy(DrawContext context) {}

    @Override
    public int getWidth() {
        return MinecraftClient.getInstance().textRenderer.getWidth("Test Element") + 4;
    }

    @Override
    public int getHeight() {
        return MinecraftClient.getInstance().textRenderer.fontHeight + 4;
    }
}
