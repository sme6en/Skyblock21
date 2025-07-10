package com.skyblock21.hud.elements;

import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.features.foraging.GalateaTracker;
import com.skyblock21.hud.HudElement;
import com.skyblock21.util.Location;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.util.Map;

public class BonusGiftsTrackerElement extends HudElement {

    public BonusGiftsTrackerElement(int x, int y) {
        super(x, y, Location.GALATEA);
    }

    @Override
    protected void renderElement(DrawContext context) {
        Map<String, Integer> bonusDrops = PersistentData.get().bonusDrops;
        if (bonusDrops.isEmpty()) return;

        MatrixStack matrices = context.getMatrices();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        // 20% bigger
        matrices.push();
        matrices.scale(1.2f, 1.2f, 1.0f);
        context.drawTextWithShadow(textRenderer, "§lBonus Gifts", 2, 2, 0xFF55FF);
        matrices.pop();


        int lineIndex = 0;
        for (Map.Entry<String, Integer> entry : bonusDrops.entrySet()) {
            String itemName = entry.getKey();
            int count = entry.getValue();
            String line = String.format("%s§а: %d", itemName, count);
            context.drawTextWithShadow(textRenderer, line, 2, 5 + (textRenderer.fontHeight + VERTICAL_PADDING) * (lineIndex + 1), Color.WHITE.getRGB());
            lineIndex++;
        }
    }

    @Override
    protected void renderDummy(DrawContext context) {
        String dummyText = GalateaTracker.getDummyBonusDropsText();
        MatrixStack matrices = context.getMatrices();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        // 20% bigger
        matrices.push();
        matrices.scale(1.2f, 1.2f, 1.0f);
        context.drawTextWithShadow(textRenderer, "§lBonus Gifts", 2, 2, 0xFF55FF);
        matrices.pop();

        String[] lines = dummyText.split("\n");
        for (int i = 0; i < lines.length; i++) {
            context.drawTextWithShadow(textRenderer, lines[i], 2, 5 + (textRenderer.fontHeight + VERTICAL_PADDING) * (i + 1), Color.WHITE.getRGB());
        }
    }

    @Override
    public int getWidth() {
        int longestString = PersistentData.get().bonusDrops.entrySet()
                                                           .stream()
                                                           .mapToInt(entry -> MinecraftClient.getInstance().textRenderer.getWidth(entry.getKey() + ": " + entry.getValue()))
                                                           .max()
                                                           .orElse(0);

        return shouldRenderDummy() ? (int) (MinecraftClient.getInstance().textRenderer.getWidth("§d§lFirst Impression I: 3") * 1.3) : (int) (Math.max(longestString, MinecraftClient.getInstance().textRenderer.getWidth("§lBonus Gifts")) * 1.3);
    }

    @Override
    public int getHeight() {
        int lineCount = shouldRenderDummy() ? 4 : PersistentData.get().bonusDrops.size() + 1;
        return (int) (((MinecraftClient.getInstance().textRenderer.fontHeight) * lineCount + VERTICAL_PADDING * (lineCount)) * 1.3);
    }

    @Override
    public boolean shouldRenderDummy() {
        return super.shouldRenderDummy() || PersistentData.get().bonusDrops.isEmpty();
    }

    @Override
    public boolean isEnabled() {
        return Skyblock21ConfigManager.get().foraging.bonusGiftsTracker;
    }
}
