package com.skyblock21.features.foraging;

import com.skyblock21.Skyblock21;
import com.skyblock21.config.Skyblock21Config;
import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.config.persistent.PersistentData;
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
import java.util.*;

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

    private static boolean insideTreeGiftMessage = false;
    private static boolean pastBonusGiftsMessage = false;
    private static void onChat(Text text) {
        if (!Utils.isOnSkyblock()) return;
        if (!Utils.isInGalatea()) return;
        Skyblock21Config config = Skyblock21ConfigManager.get();
        if (!config.foraging.bonusGiftsTracker && !config.foraging.galateaTracker) return;

        String message = text.getString();
        if (message.equals("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")) {
            if (!insideTreeGiftMessage) {
                insideTreeGiftMessage = true;
            } else {
                insideTreeGiftMessage = false;
                pastBonusGiftsMessage = false;
            }
        }
        if (!message.contains("rewards gained") && !insideTreeGiftMessage && !message.contains("BONUS GIFT")) return;

        if (message.contains("rewards gained")) {
            parseExp(text);
        } else {
            parseBonusGifts(text);
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

    private static void parseBonusGifts(Text text) {
        String message = text.getString();

        if (message.contains("BONUS GIFT") && !pastBonusGiftsMessage) {
            pastBonusGiftsMessage = true;
        }

        if (!message.endsWith("%)") || !pastBonusGiftsMessage) return;

        String itemName = TextUtils.toLegacy(text).replaceAll("\\s§8\\(§a([\\d.]+%)§8\\)", "").replaceAll("^§f\\s+", "");

        if (itemName.startsWith("Enchanted Book")) {
            // Extract text inside the first parentheses
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("§aEnchanted Book \\((.*?)§a\\)")
                                                                     .matcher(itemName);
            if (matcher.find()) {
                itemName = matcher.group(1).trim();
            }

            if (itemName.isEmpty()) {
                Skyblock21.LOGGER.error("GalateaTracker - Error parsing Enchanted Book name from message: " + message);
                return;
            }


        }
        PersistentData.get().bonusDrops.put(itemName, PersistentData.get().bonusDrops.getOrDefault(itemName, 0) + 1);

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(PersistentData.get().bonusDrops.entrySet());

        entries.sort((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()));

        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : entries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
    }

    private static void parseExp(Text text) {
        boolean shouldUpdateRate = false;

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

    public static String getDummyBonusDropsText() {
        return """
                §d§lFirst Impression I: 3
                §aStretching Sticks: 6
                §6Chameleon: 1""";
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

    public static void resetBonusGifts() {
        PersistentData.get().bonusDrops.clear();
        TextUtils.addMessage("§aBonus Gifts tracker reset!", true, false);
    }
}
