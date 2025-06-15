package com.skyblock21.features.foraging;

import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.events.ChatEvents;
import com.skyblock21.util.TextUtils;
import com.skyblock21.util.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

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

    private static void onChat(Text text) {
        if (!Utils.isOnSkyblock()) return;
        if (!Utils.isInGalatea()) return;

        String message = text.getString();
        if (!message.contains("rewards gained")) return;


        Style style = text.getStyle();
        if (style == null || style.getHoverEvent() == null) return;

        HoverEvent hover = style.getHoverEvent();
        if (hover.getAction() != HoverEvent.Action.SHOW_TEXT) return;

        Text hoverText = ((HoverEvent.ShowText) hover).value();

        for (Text child : hoverText.getSiblings()) {
            String line = child.getString().trim();

            if (line.contains("Forest Essence")) {
                totalForestEssence += extractNumber(line);
            } else if (line.contains("Foraging Experience")) {
                totalForagingExp += extractNumber(line);
            } else if (line.contains("HOTF Experience")) {
                totalHOTFExperience += extractNumber(line);
            } else if (line.contains("Forest Whispers")) {
                totalWhispers += extractNumber(line);
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
    }

    private static int extractNumber(String line) {
        Pattern p = Pattern.compile("x([\\d,]+)");
        Matcher m = p.matcher(line);
        if (m.find()) {
            return Integer.parseInt(m.group(1).replace(",", ""));
        }
        return 0;
    }

    private static void onTick(MinecraftClient client) {
        if (!Utils.isOnSkyblock()) return;
        if (!Utils.isInGalatea()) return;
        if (sessionStartTime == -1) return;

        long currentTime = System.currentTimeMillis();
        if (!isAfk && lastActionTime != -1 && currentTime - lastActionTime > Skyblock21ConfigManager.get().foraging.afkTimeout) {
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

    public static String getHudText() {
        if (sessionStartTime == -1) return "";

        long now = isAfk ? afkStartTime : System.currentTimeMillis();
        double activeHours = (now - sessionStartTime - totalAfkTime) / 3600000.0;
        double whispersPerHour = activeHours > 0 ? totalWhispers / activeHours : 0;

        return String.format("""
                        §8Forest Essence: §f%d
                        §3Foraging Exp: §f%,d
                        §aHOTF Exp: §f%d
                        §2Whispers: §f%d §8(%.0f/hr)
                        %s
                        """,
                totalForestEssence,
                totalForagingExp,
                totalHOTFExperience,
                totalWhispers,
                whispersPerHour,
                isAfk ? "§c(Paused)" : ""
        );
    }

    public static String getDummyHudText() {
        return "§8Forest Essence: §f90\n" +
                "§3Foraging Exp: §f2,500\n" +
                "§aHOTF Exp: §f2,500\n" +
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
