package com.skyblock21.tracking.impl;

import com.skyblock21.Skyblock21;
import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.events.ChatEvents;
import com.skyblock21.events.SkyblockEvents;
import com.skyblock21.tracking.*;
import com.skyblock21.util.Location;
import com.skyblock21.util.TextUtils;
import com.skyblock21.util.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.Optional;

public class GalateaTracker extends BaseTracker {

    private boolean insideTreeGiftMessage = false;
    private boolean pastBonusGiftsMessage = false;

    public GalateaTracker() {
        super(
                "galatea_tracker",
                new GalateaTrackerConditions(),
                TrackerSettings.withPersistence(Location.GALATEA)
        );

        ChatEvents.RECEIVE_TEXT.register(this::onChat);
        SkyblockEvents.SKILL_GAINED.register(this::onSkillGained);
        SkyblockEvents.LOCATION_CHANGE.register(this::onLocationChange);
    }

    @Override
    protected void onTick(MinecraftClient client) {
        // AFK detection is handled by base tracker
    }

    @Override
    protected void onValueTracked(String key, Number increment, TrackableValue<?> value) {
        // Values are automatically tracked
    }

    @Override
    protected void onSessionReset() {
        TextUtils.addMessage("§aGalatea session reset!", true, false);
    }

    @Override
    protected void onAllDataReset() {
        TextUtils.addMessage("§aAll Galatea data reset!", true, false);
    }

    private void onChat(Text text) {
        if (!Utils.isOnSkyblock() || !Utils.isInGalatea()) return;
        if (!conditions.shouldTrack(this)) return;

        String message = text.getString();

        // Handle tree gift message boundaries
        if (message.equals("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")) {
            if (!insideTreeGiftMessage) {
                insideTreeGiftMessage = true;
            } else {
                insideTreeGiftMessage = false;
                pastBonusGiftsMessage = false;
            }
        }

        if (!message.contains("rewards gained") && !insideTreeGiftMessage && !message.contains("BONUS GIFT")) {
            return;
        }

        if (message.contains("rewards gained")) {
            parseExp(text);
        } else {
            parseBonusGifts(text);
        }
    }

    private void onSkillGained(SkyblockEvents.Skill skill, double amount) {
        if (skill != SkyblockEvents.Skill.FORAGING || !conditions.shouldTrack(this)) return;
        trackValue("foraging_exp", (int) amount);
    }

    private void onLocationChange(Location location) {
        if (!Utils.isOnSkyblock() || !Utils.isInGalatea()) {
            pauseTracker();
        }
    }

    private void parseBonusGifts(Text text) {
        String message = text.getString();

        if (message.contains("BONUS GIFT") && !pastBonusGiftsMessage) {
            pastBonusGiftsMessage = true;
        }

        if (!message.endsWith("%)") || !pastBonusGiftsMessage) return;

        String itemName = TextUtils.toLegacy(text)
                                   .replaceAll("\\s§8\\(§a([\\d.]+%)§8\\)", "")
                                   .replaceAll("^§f\\s+", "");

        if (itemName.startsWith("Enchanted Book")) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern
                    .compile("§aEnchanted Book \\((.*?)§a\\)")
                    .matcher(itemName);
            if (matcher.find()) {
                itemName = matcher.group(1).trim();
            }

            if (itemName.isEmpty()) {
                Skyblock21.LOGGER.error("GalateaTracker - Error parsing Enchanted Book name from message: " + message);
                return;
            }
        }

        BaseTracker bonusGiftsTracker = TrackerManager.getTrackerById("bonus_gifts_tracker").get();
        bonusGiftsTracker.trackValue("gift_" + itemName, 1);
    }

    private void parseExp(Text text) {
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

        // Check for Forest Essence in first line
        if (hoverText.toString().trim().contains("Forest Essence")) {
            String nextLine = hoverText.getSiblings().getFirst().getString().trim();
            if (!nextLine.isEmpty()) {
                trackValue("forest_essence", parseNumber(nextLine));
            }
        }

        for (Text child : hoverText.getSiblings()) {
            String line = child.getString().trim();
            int index = hoverText.getSiblings().indexOf(child);

            if (index == -1 || index == hoverText.getSiblings().size() - 1) continue;

            String nextChild = hoverText.getSiblings().get(index + 1).getString();

            if (line.contains("Foraging Experience")) {
                trackValue("foraging_exp", parseNumber(nextChild));
            } else if (line.contains("HOTF Experience")) {
                trackValue("hotf_exp", parseNumber(nextChild));
            } else if (line.contains("Forest Whispers")) {
                trackValue("whispers", parseNumber(nextChild));
            }
        }
    }

    private int parseNumber(String str) {
        str = str.replace(",", "").replace("x", "").trim();
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            Skyblock21.LOGGER.error("GalateaTracker - Error parsing number: " + str);
            return 0;
        }
    }

    private static class GalateaTrackerConditions implements TrackerConditions {

        @Override
        public boolean shouldTrack(BaseTracker tracker) {
            return Utils.isOnSkyblock() && Utils.isInGalatea();
        }

        @Override
        public boolean shouldRender(BaseTracker tracker) {
            return Skyblock21ConfigManager.get().foraging.galateaTracker;
        }
    }
}