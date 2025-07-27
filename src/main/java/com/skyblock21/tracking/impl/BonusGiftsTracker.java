package com.skyblock21.tracking.impl;

import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.events.ChatEvents;
import com.skyblock21.events.SkyblockEvents;
import com.skyblock21.tracking.BaseTracker;
import com.skyblock21.tracking.TrackerConditions;
import com.skyblock21.tracking.TrackerSettings;
import com.skyblock21.tracking.TrackableValue;
import com.skyblock21.util.Location;
import com.skyblock21.util.TextUtils;
import com.skyblock21.util.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

public class BonusGiftsTracker extends BaseTracker {

    private Map<String, String> giftNameMapping = new HashMap<>();

    public BonusGiftsTracker() {
        super(
                "bonus_gifts_tracker",
                new BonusGiftsTrackerConditions(),
                TrackerSettings.withPersistence(Location.GALATEA)
        );

        // Register for chat events to track bonus gifts
        ChatEvents.RECEIVE_TEXT.register(this::onChat);
        SkyblockEvents.LOCATION_CHANGE.register(this::onLocationChange);
    }

    @Override
    protected void onTick(MinecraftClient client) {
        // Base tracker handles AFK and time tracking automatically
    }

    @Override
    protected void onValueTracked(String key, Number increment, TrackableValue<?> value) {
        // Update legacy persistent data when tracking new gifts for compatibility
        if (key.startsWith("gift_")) {
            String giftName = giftNameMapping.get(key);
            if (giftName != null) {
                // Update the legacy bonusDrops for compatibility with old system
                PersistentData.get().bonusDrops.put(giftName, value.getTotal().intValue());
                PersistentData.save();
            }
        }
    }

    @Override
    protected void onSessionReset() {
        TextUtils.addMessage("§aBonus gifts session reset!", true, false);
    }

    @Override
    protected void onAllDataReset() {
        giftNameMapping.clear();
        PersistentData.get().bonusDrops.clear();
        PersistentData.save();
        TextUtils.addMessage("§aAll bonus gifts data reset!", true, false);
    }

    @Override
    protected void loadPersistentData() {
        super.loadPersistentData();

        if (giftNameMapping == null) {
            giftNameMapping = new HashMap<>();
        }

        // Load gift name mappings from persistent data
        Map<String, Integer> bonusDrops = PersistentData.get().bonusDrops;
        for (String giftName : bonusDrops.keySet()) {
            String key = "gift_" + giftName;
            giftNameMapping.put(key, giftName);
        }
    }

    private void onChat(Text text) {
        if (!Utils.isOnSkyblock() || !Utils.isInGalatea()) return;
        if (!conditions.shouldTrack(this)) return;

        // This will be called by the GalateaTracker chat parsing
        // For now, it's a placeholder for direct integration
    }

    private void onLocationChange(Location location) {
        if (!Utils.isOnSkyblock() || !Utils.isInGalatea()) {
            pauseTracker();
        }
    }

    public void trackGift(String giftName, int amount) {
        String key = "gift_" + giftName;
        giftNameMapping.put(key, giftName);
        trackValue(key, amount);
    }

    public Map<String, Integer> getCurrentGiftCounts() {
        Map<String, Integer> counts = new HashMap<>();
        Map<String, TrackableValue<?>> values = getAllValues();

        for (Map.Entry<String, TrackableValue<?>> entry : values.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("gift_")) {
                String giftName = giftNameMapping.get(key);
                if (giftName != null) {
                    counts.put(giftName, entry.getValue().asInt());
                }
            }
        }
        return counts;
    }

    public Map<String, Integer> getSessionGiftCounts() {
        Map<String, Integer> counts = new HashMap<>();

        for (Map.Entry<String, TrackableValue<?>> entry : trackedValues.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("gift_")) {
                String giftName = giftNameMapping.get(key);
                if (giftName != null) {
                    counts.put(giftName, entry.getValue().asInt());
                }
            }
        }
        return counts;
    }

    public void resetAllGifts() {
        resetAll();
        giftNameMapping.clear();
        PersistentData.get().bonusDrops.clear();
        PersistentData.save();
    }

    private static class BonusGiftsTrackerConditions implements TrackerConditions {

        @Override
        public boolean shouldTrack(BaseTracker tracker) {
            return Utils.isOnSkyblock() && Utils.isInGalatea();
        }

        @Override
        public boolean shouldRender(BaseTracker tracker) {
            return Skyblock21ConfigManager.get().foraging.bonusGiftsTracker;
        }
    }
}