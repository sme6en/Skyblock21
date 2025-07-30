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

    private Map<String, Integer> gifts = new HashMap<>();


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
        if (key.startsWith("gift_")) {
            String giftName = key.substring(5);
            gifts.put(giftName, value.getTotal().intValue());
        }
    }

    @Override
    protected void onSessionReset() {
        gifts.clear();
        TextUtils.addMessage("§aBonus gifts session reset!", true, false);
    }

    @Override
    protected void onAllDataReset() {
        gifts.clear();
        TextUtils.addMessage("§aAll bonus gifts data reset!", true, false);
    }

    @Override
    protected void loadPersistentData() {
        super.loadPersistentData();
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