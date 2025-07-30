package com.skyblock21.hud.elements;

import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.features.foraging.TreeProgress;
import com.skyblock21.hud.EditGuiScreen;
import com.skyblock21.hud.EditHudElementScreen;
import com.skyblock21.hud.HudElement;
import com.skyblock21.util.Location;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.awt.*;

public class TreeProgressHudElement extends HudElement {

    public TreeProgressHudElement(int x, int y) {
        super(x, y, Location.GALATEA, true);
    }

    @Override
    protected void renderElement(DrawContext context) {
        boolean isEditMode = MinecraftClient.getInstance().currentScreen instanceof EditGuiScreen ||
                MinecraftClient.getInstance().currentScreen instanceof EditHudElementScreen;
        if (shouldRenderDummy() && isEditMode) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            context.drawItem(new ItemStack(Items.STRIPPED_SPRUCE_LOG), 2, 2);
            context.drawTextWithShadow(textRenderer, "20%", 2 + 16 + 2, 7, Color.GREEN.getRGB());
        } else {
            TreeProgress.render(context, 2, 2);
        }

    }

    @Override
    public int getWidth() {
        return (int) ((MinecraftClient.getInstance().textRenderer.getWidth("§a20%") + 22) * 1.3f);
    }

    @Override
    public int getHeight() {
        return 20;
    }

    @Override
    public boolean isEnabled() {
        return Skyblock21ConfigManager.get().foraging.treeProgress && MinecraftClient.getInstance().currentScreen == null || !(MinecraftClient.getInstance().currentScreen instanceof HandledScreen);
    }

    @Override
    public boolean shouldRenderDummy() {
        return super.shouldRenderDummy() && TreeProgress.currentEntity == null;
    }
}
