package com.skyblock21.hud.elements;

import com.skyblock21.features.kuudra.Kuudra;
import com.skyblock21.gui.ThemeManager;
import com.skyblock21.hud.HudElement;
import com.skyblock21.util.Render2DUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class TestHudElement extends HudElement {

    public TestHudElement(int x, int y) {
        super(x, y);
    }

    @Override
    protected void renderElement(DrawContext context) {
        TextRenderer textRenderer = ThemeManager.getCurrentTheme().getTextRenderer();
//        context.drawTextWithShadow(textRenderer, "Current phase: ", 2, 2, 0xff0000);
        Render2DUtil.drawString(context, "Hello", getX(), getY(), ThemeManager.getCurrentTheme().text);
////        context.drawTextWithShadow(textRenderer, "Whisps: " + HOTFOverlay.whisperAmount, 2, 2 + textRenderer.fontHeight + 2, 0xFFFFFF);
//        Render2DUtil.drawRoundedBox(context, getX(), getY(), getWidth(), getHeight(), 10f, new Color(0,0,255,255));
//        Render2DUtil.drawRoundedBoxOutline(context, getX(), getY(), getWidth(), getHeight(), 10f, new Color(0,0,255,255));
//        Render2DUtil.drawBox(context, getX() + 2, getY() + 2 + textRenderer.fontHeight + 2, getWidth() - 4, textRenderer.fontHeight, new Color(0,0,255,255));
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
