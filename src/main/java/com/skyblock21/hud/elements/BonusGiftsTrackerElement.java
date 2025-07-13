package com.skyblock21.hud.elements;

import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.features.foraging.GalateaTracker;
import com.skyblock21.hud.HudElement;
import com.skyblock21.hud.MultiLineHudElement;
import com.skyblock21.hud.SortType;
import com.skyblock21.util.Location;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.util.Map;

public class BonusGiftsTrackerElement extends MultiLineHudElement {

    public static BonusGiftsTrackerElement INSTANCE;

    public BonusGiftsTrackerElement(int x, int y) {
        super(x, y, Location.GALATEA);

        if (INSTANCE == null) {
            INSTANCE = this;
        }

        createGroup("gifts", "Bonus Gifts", true);
        setGroupSorting("gifts", SortType.AMOUNT, false);
    }

    @Override
    protected void renderElement(DrawContext context) {
        MatrixStack matrices = context.getMatrices();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        // 20% bigger
        matrices.push();
        matrices.scale(1.2f, 1.2f, 1.0f);
        context.drawTextWithShadow(textRenderer, "§lBonus Gifts", 2, 2, 0xFF55FF);
        matrices.pop();

        super.renderElement(context);
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

        super.renderElement(context);
    }

//    @Override
//    public int getWidth() {
//        int longestString = PersistentData.get().bonusDrops.entrySet()
//                                                           .stream()
//                                                           .mapToInt(entry -> MinecraftClient.getInstance().textRenderer.getWidth(entry.getKey() + ": " + entry.getValue()))
//                                                           .max()
//                                                           .orElse(0);
//
//        return shouldRenderDummy() ? (int) (MinecraftClient.getInstance().textRenderer.getWidth("§d§lFirst Impression I: 3") * 1.3) : (int) (Math.max(longestString, MinecraftClient.getInstance().textRenderer.getWidth("§lBonus Gifts")) * 1.3);
//    }
//
//    @Override
//    public int getHeight() {
//        int lineCount = shouldRenderDummy() ? 4 : PersistentData.get().bonusDrops.size() + 1;
//        return (int) (((MinecraftClient.getInstance().textRenderer.fontHeight) * lineCount + VERTICAL_SPACING * (lineCount)) * 1.3);
//    }

    @Override
    public boolean shouldRenderDummy() {
        return super.shouldRenderDummy() || PersistentData.get().bonusDrops.isEmpty();
    }

    @Override
    public boolean isEnabled() {
        return Skyblock21ConfigManager.get().foraging.bonusGiftsTracker;
    }
}
