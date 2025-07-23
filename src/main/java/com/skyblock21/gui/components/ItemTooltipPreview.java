package com.skyblock21.gui.components;

import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

public class ItemTooltipPreview extends BaseComponent {

    private final ItemStack itemStack;

    public ItemTooltipPreview(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        context.drawItemTooltip(textRenderer, itemStack, x, y);
    }

    @Override
    protected int determineHorizontalContentSize(Sizing sizing) {
        int longest = 0;
        MinecraftClient client = MinecraftClient.getInstance();
        List<Text> tooltipItems = Screen.getTooltipFromItem(client, itemStack);
        for (Text t : tooltipItems) {
            int length = client.textRenderer.getWidth(t);
            if (length > longest) {
                longest = length;
            }
        }

        return longest;
    }

    @Override
    protected int determineVerticalContentSize(Sizing sizing) {
        MinecraftClient client = MinecraftClient.getInstance();
        List<Text> tooltipItems = Screen.getTooltipFromItem(client, itemStack);

        return (client.textRenderer.fontHeight * tooltipItems.size()) + tooltipItems.size() == 0 ? 2 : 0;
    }
}
