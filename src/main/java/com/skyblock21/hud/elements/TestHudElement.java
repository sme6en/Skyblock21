package com.skyblock21.hud.elements;

import com.skyblock21.features.foraging.HOTFOverlay;
import com.skyblock21.features.kuudra.Kuudra;
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
//        context.drawTextWithShadow(textRenderer, "Current phase: " + Kuudra.currentPhase, 2, 2, 0xFFFFFF);
//        context.drawTextWithShadow(textRenderer, "Whisps: " + HOTFOverlay.whisperAmount, 2, 2 + textRenderer.fontHeight + 2, 0xFFFFFF);
    }

    @Override
    protected void renderDummy(DrawContext context) {}

    @Override
    public int getWidth() {
        return MinecraftClient.getInstance().textRenderer.getWidth("Current phase: " + Kuudra.currentPhase) + 4;
    }

    @Override
    public int getHeight() {
        return MinecraftClient.getInstance().textRenderer.fontHeight * 2 + 6;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
