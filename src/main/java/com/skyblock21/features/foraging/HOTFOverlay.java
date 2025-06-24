package com.skyblock21.features.foraging;

import com.skyblock21.events.WindowEvents;
import com.skyblock21.util.TextUtils;
import com.skyblock21.util.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.List;
import java.util.regex.Pattern;

public class HOTFOverlay {

    private static final Pattern WHISPER_UPGRADE_PATTERN = Pattern.compile("^\\s*([\\d,]+) Forest Whispers");
    public static int whisperAmount = 0;

    public static void init() {
        WindowEvents.DRAW_BEFORE_ITEM.register(HOTFOverlay::onDrawSlot);

        ClientTickEvents.END_CLIENT_TICK.register(HOTFOverlay::onTick);
    }

    private static void onTick(MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        if (!Utils.isOnSkyblock()) return;

        ClientPlayNetworkHandler handler = client.getNetworkHandler();
        if (handler != null) {
            whisperAmount = handler.getPlayerList().stream()
                .map(PlayerListEntry::getDisplayName)
                .filter(dn -> dn != null)
                .map(dn -> dn.getString())
                .filter(s -> s.contains("Forest Whispers: "))
                .map(s -> s.replace("Forest Whispers: ", "").replace(",", "").trim())
                .mapToInt(s -> {
                    try { return TextUtils.parseIntWithSuffix(s); } catch (NumberFormatException e) { return 0; }
                })
                .findFirst()
                .orElse(0);
        }
    }

    private static void onDrawSlot(Text text, DrawContext drawContext, Slot slot) {
        List<Text> lines = slot.getStack().getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).lines();

        boolean enoughWhispers = !lines.stream().anyMatch(l -> l.contains(Text.literal("You don't have enough Forest Whispers")));

        if (!enoughWhispers || whisperAmount <= 0) {
            return;
        }

        for (Text line : lines) {
            if (WHISPER_UPGRADE_PATTERN.matcher(line.getString()).matches()) {
                String amount = line.getString().replace(",", "").replace("Forest Whispers", "").trim();
                int upgradeAmount = Integer.parseInt(amount);
                if (upgradeAmount <= whisperAmount) {
                    drawContext.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, Color.GREEN.getRGB());
                } else {
                    drawContext.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, Color.RED.getRGB());
                }
            }
        }
    }
}
