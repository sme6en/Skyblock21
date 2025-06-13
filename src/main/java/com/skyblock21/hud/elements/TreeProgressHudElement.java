package com.skyblock21.hud.elements;

import com.skyblock21.features.foraging.TreeProgress;
import com.skyblock21.hud.HudElement;
import com.skyblock21.util.Location;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.awt.*;

public class TreeProgressHudElement extends HudElement {

    public TreeProgressHudElement(int x, int y) {
        super(x, y, Location.GALATEA, true);
    }

    @Override
    protected void renderElement(DrawContext context) {
        TreeProgress.render(context, 2, 2);
    }

    public void renderDummy(DrawContext context) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        context.drawItem(new ItemStack(Items.STRIPPED_SPRUCE_LOG), 2, 2);
        context.drawTextWithShadow(textRenderer, "20%", 2 + 16 + 2, 7, Color.GREEN.getRGB());
    }

    @Override
    public int getWidth() {
        return MinecraftClient.getInstance().textRenderer.getWidth("§a20%") + 22;
    }

    @Override
    public int getHeight() {
        return 20;
    }
}
