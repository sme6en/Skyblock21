package com.skyblock21.features.foraging;

import com.skyblock21.Skyblock21;
import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.events.ChatEvents;
import com.skyblock21.util.TextUtils;
import com.skyblock21.util.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GalateaTracker {

    private static long sessionStartTime = -1;
    private static long lastActionTime = -1;
    private static long afkStartTime = -1;
    private static long totalAfkTime = 0;

    private static int totalWhispers = 0;
    private static int totalForagingExp = 0;
    private static int totalForestEssence = 0;
    private static int totalHOTFExperience = 0;

    private static boolean isAfk = false;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(GalateaTracker::onTick);
        ChatEvents.RECEIVE_TEXT.register(GalateaTracker::onChat);
    }

    private static float lastYaw = 0f;
    private static float lastPitch = 0f;
    private static BlockPos lastPosition = null;

    private static double cachedWhispersPerHour = 0;
    private static double cachedForagingExpPerHour = 0;
    private static double cachedHOTFExpPerHour = 0;

    private static void onChat(Text text) {
        if (!Utils.isOnSkyblock()) return;
        if (!Utils.isInGalatea()) return;

        boolean shouldUpdateRate = false;

        String message = text.getString();
        if (!message.contains("rewards gained")) return;

        Style style = text.getStyle();
        for (Text sibling : text.getSiblings()) {

            if (sibling.getStyle() != null) {
                Style siblingStyle = sibling.getStyle();
                if (siblingStyle.getHoverEvent() != null) {
                    style = siblingStyle;
                    break;
                }
            }
        }

        if (style == null || style.getHoverEvent() == null) return;

        HoverEvent hover = style.getHoverEvent();
        if (hover.getAction() != HoverEvent.Action.SHOW_TEXT) return;

        Text hoverText = ((HoverEvent.ShowText) hover).value();

        if (hoverText == null || hoverText.getSiblings().isEmpty()) return;

        if (hoverText.toString().trim().contains("Forest Essence")) {
            String nextLine = hoverText.getSiblings().getFirst().getString().trim();

            if (nextLine.isEmpty()) return;

            totalForestEssence += parseNumber(nextLine);
            shouldUpdateRate = true;
        }

        for (Text child : hoverText.getSiblings()) {
            String line = child.getString().trim();

            int index = hoverText.getSiblings().indexOf(child);
            if (index == -1 || index == hoverText.getSiblings().size() - 1) continue;

            String nextChild = hoverText.getSiblings().get(index + 1).getString();
            if (line.contains("Foraging Experience")) {
                totalForagingExp += parseNumber(nextChild);
                shouldUpdateRate = true;
            } else if (line.contains("HOTF Experience")) {
                totalHOTFExperience += parseNumber(nextChild);
                shouldUpdateRate = true;
            } else if (line.contains("Forest Whispers")) {
                totalWhispers += parseNumber(nextChild);
                shouldUpdateRate = true;
            }
        }

        long currentTime = System.currentTimeMillis();
        if (sessionStartTime == -1) {
            sessionStartTime = currentTime;
        }
        lastActionTime = currentTime;
        if (isAfk) {
            exitAfk();
        }

        if (shouldUpdateRate) {
            updateRatesPerHour();
        }
    }

    private static int parseNumber(String str) {
        str = str.replace(",", "").replace("x","").trim();
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            Skyblock21.LOGGER.error("GalateaTracker - Error parsing number: " + str);

            return 0;
        }
    }

    private static void onTick(MinecraftClient client) {
        if (client.world == null || client.player == null) return;
        if (!Utils.isOnSkyblock()) return;
        if (!Utils.isInGalatea()) return;
        if (sessionStartTime == -1) return;

        float currentYaw = client.player.getYaw();
        float currentPitch = client.player.getPitch();
        BlockPos currentPosition = client.player.getBlockPos();

        if (currentPitch != lastPitch || currentYaw != lastYaw || !currentPosition.equals(lastPosition)) {
            lastActionTime = System.currentTimeMillis();
            lastYaw = currentYaw;
            lastPitch = currentPitch;
            lastPosition = currentPosition;
            if (isAfk) {
                exitAfk();
            }
        }

        long currentTime = System.currentTimeMillis();
        if (!isAfk && lastActionTime != -1 && currentTime - lastActionTime > Skyblock21ConfigManager.get().foraging.afkTimeout * 1000) {
            isAfk = true;
            afkStartTime = currentTime;
        }
    }

    private static void exitAfk() {
        if (!isAfk || afkStartTime == -1) return;

        long currentTime = System.currentTimeMillis();
        totalAfkTime += currentTime - afkStartTime;
        isAfk = false;
    }

    private static void updateRatesPerHour() {
        if (sessionStartTime == -1) {
            cachedWhispersPerHour = 0;
            cachedForagingExpPerHour = 0;
            cachedHOTFExpPerHour = 0;
            cachedForagingExpPerHour = 0;
            cachedHOTFExpPerHour = 0;
            return;
        }

        long currentTime = System.currentTimeMillis();
        long currentAfkTime = isAfk && afkStartTime != -1 ? totalAfkTime + (currentTime - afkStartTime) : totalAfkTime;
        double activeHours = (currentTime - sessionStartTime - currentAfkTime) / 3600000.0;

        if (activeHours > 0) {
            cachedWhispersPerHour = Math.round(totalWhispers / activeHours);
            cachedForagingExpPerHour = Math.round(totalForagingExp / activeHours);
            cachedHOTFExpPerHour = Math.round(totalHOTFExperience / activeHours);
        } else {
            cachedWhispersPerHour = 0;
            cachedForagingExpPerHour = 0;
            cachedHOTFExpPerHour = 0;
        }
    }

    public static String getHudText() {
        if (sessionStartTime == -1) return "";
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);

        return String.format("""
                        §8Forest Essence: §f%d
                        §3Foraging Exp: §f%,d §8(%s/hr)
                        §aHOTF Exp: §f%d §8(%s/hr)
                        §2Whispers: §f%d §8(%s/hr)
                        %s
                        """,
                totalForestEssence,
                totalForagingExp,
                formatter.format(cachedForagingExpPerHour),
                totalHOTFExperience,
                formatter.format(cachedHOTFExpPerHour),
                totalWhispers,
                formatter.format(cachedWhispersPerHour),
                isAfk ? "§c(Paused)" : ""
        );
    }

    public static String getDummyHudText() {
        return "§8Forest Essence: §f90 §8(800/hr)\n" +
                "§3Foraging Exp: §f2,500 §8(2,000/hr)\n" +
                "§aHOTF Exp: §f2,500 §8(14,203/hr)\n" +
                "§2Whispers: §f5,000 §8(80,000/hr)\n" +
                "§c(Paused)";
    }

    public static void resetSession() {
        sessionStartTime = -1;
        lastActionTime = -1;
        afkStartTime = -1;
        totalAfkTime = 0;
        isAfk = false;

        totalWhispers = 0;
        totalForagingExp = 0;
        totalForestEssence = 0;
        totalHOTFExperience = 0;

        TextUtils.addMessage("§aGalatea session reset!", true, false);
    }
}
