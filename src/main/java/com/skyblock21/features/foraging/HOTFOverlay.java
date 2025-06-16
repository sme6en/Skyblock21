package com.skyblock21.features.foraging;

import com.skyblock21.events.WindowEvents;
import com.skyblock21.util.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.List;
import java.util.regex.Pattern;

public class HOTFOverlay {

    private static final Pattern WHISPER_PATTERN = Pattern.compile("^Forest Whispers:\\s*([\\d,]+)");
    private static final Pattern WHISPER_UPGRADE_PATTERN = Pattern.compile("^\\s*([\\d,]+) Forest Whispers");
    private static int whisperAmount = 0;

    public static void init() {
        WindowEvents.DRAW_BEFORE_ITEM.register(HOTFOverlay::onDrawSlot);

        ClientTickEvents.END_CLIENT_TICK.register(HOTFOverlay::onTick);
    }

    private static void onTick(MinecraftClient client) {
        if (!Utils.isOnSkyblock()) return;
        if (client.currentScreen == null || !(client.currentScreen instanceof HandledScreen<?>)) return;
        HandledScreen<?> screen = (HandledScreen<?>) client.currentScreen;
        if (!screen.getTitle().getString().startsWith("Heart of the Forest")) return;

        // 49th slot
        Slot slot = screen.getScreenHandler().getSlot(49);
        if (slot.getStack().isEmpty()) {
            return;
        }
        Text lastLines = slot.getStack().getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).lines().getLast();

        if (WHISPER_PATTERN.matcher(lastLines.getString()).matches()) {
            String amount = lastLines.getString().replace("Forest Whispers: ", "").replace(",", "").trim();
            whisperAmount = Integer.parseInt(amount);
            System.out.println("Whisper Amount: " + whisperAmount);
        }
    }

    private static void onDrawSlot(Text text, DrawContext drawContext, Slot slot) {
        List<Text> lines = slot.getStack().getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).lines();

        for (Text line : lines) {
            if (WHISPER_UPGRADE_PATTERN.matcher(line.getString()).matches()) {
                String amount = line.getString().replace(",", "").replace("Forest Whispers", "").trim();
                int upgradeAmount = Integer.parseInt(amount);
                if (upgradeAmount <= whisperAmount) {
                    drawContext.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, Color.GREEN.getRGB());
                }
            }
        }
    }
}
