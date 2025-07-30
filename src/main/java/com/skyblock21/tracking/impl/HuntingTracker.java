package com.skyblock21.tracking.impl;

import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.tracking.BaseTracker;
import com.skyblock21.tracking.TrackerSettings;
import com.skyblock21.tracking.TrackerConditions;
import com.skyblock21.tracking.TrackableValue;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.events.ChatEvents;
import com.skyblock21.events.SkyblockEvents;
import com.skyblock21.util.Location;
import com.skyblock21.util.TextUtils;
import com.skyblock21.util.Utils;
import net.minecraft.text.Text;

import java.time.Period;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HuntingTracker extends BaseTracker {

    private static final Pattern GENERAL_SHARD_PATTERN = Pattern.compile("(?:a|(\\d+)) (.*?) §.Shard");

    //those can be combined into one pattern but cba rn
    private static final Pattern SALT_SHARD_PATTERN = Pattern.compile("SALT You charmed a .* and captured (\\d+) Shards? from it\\.");
    private static final Pattern CHARM_SHARD_PATTERN = Pattern.compile("CHARM You charmed a .* and captured (\\d+) Shards? from it\\.");
    private static final Pattern NAGA_SHARD_PATTERN = Pattern.compile("NAGA You charmed a .* and captured (\\d+) shards? from it\\.");

    private Map<String, Integer> shardCounts = new HashMap<>();

    public HuntingTracker() {
        super(
                "hunting_tracker",
                new HuntingTrackerConditions(),
                TrackerSettings.withPersistence()
        );

        ChatEvents.RECEIVE_TEXT.register(this::onChat);
        SkyblockEvents.SKILL_GAINED.register(this::onSkillGained);
        SkyblockEvents.LOCATION_CHANGE.register(this::onLocationChange);
    }

    @Override
    protected void onTick(MinecraftClient client) {
    }

    @Override
    protected void onValueTracked(String key, Number increment, TrackableValue<?> value) {
        if (key.startsWith("shard_")) {
            String shardName = key.substring(6);
            shardCounts.put(shardName, value.getTotal().intValue());
        }
    }

    @Override
    protected void onSessionReset() {
        shardCounts.clear();
        TextUtils.addMessage("§aHunting session reset!", true, false);
    }

    @Override
    protected void onAllDataReset() {
        shardCounts.clear();
        TextUtils.addMessage("§aAll hunting data reset!", true, false);
    }

    private void onChat(Text text) {
        if (!Utils.isOnSkyblock()) return;
        if (!conditions.shouldTrack(this)) return;

        String message = text.getString();

        if (checkSpecialShards(message)) {
            return;
        }

        Matcher matcher = GENERAL_SHARD_PATTERN.matcher(message);
        if (matcher.find()) {
            String amount = matcher.group(1);
            String shardName = matcher.group(2);
            int count = (amount != null) ? Integer.parseInt(amount) : 1;

            trackShard(shardName, count);
        }
    }

    private boolean checkSpecialShards(String message) {
        Matcher saltMatcher = SALT_SHARD_PATTERN.matcher(message);
        if (saltMatcher.find()) {
            int count = Integer.parseInt(saltMatcher.group(1));
            trackShard("§dSalt", count);
            return true;
        }

        Matcher charmMatcher = CHARM_SHARD_PATTERN.matcher(message);
        if (charmMatcher.find()) {
            int count = Integer.parseInt(charmMatcher.group(1));
            trackShard("§5Charm", count);
            return true;
        }

        Matcher nagaMatcher = NAGA_SHARD_PATTERN.matcher(message);
        if (nagaMatcher.find()) {
            int count = Integer.parseInt(nagaMatcher.group(1));
            trackShard("§6Naga", count);
            return true;
        }

        return false;
    }

    private void onSkillGained(SkyblockEvents.Skill skill, double amount) {
        if (skill != SkyblockEvents.Skill.HUNTING || !conditions.shouldTrack(this)) return;
        trackValue("hunting_exp", (int) amount);
    }

    private void onLocationChange(Location location) {
        if (!Utils.isOnSkyblock()) {
            pauseTracker();
        }
    }

    public void trackHuntingExp(int amount) {
        trackValue("hunting_exp", amount);
    }

    public void trackShard(String shardName, int amount) {
        trackValue("shard_" + shardName, amount);
    }

    public Map<String, Integer> getShardCounts() {
        return new HashMap<>(shardCounts);
    }

    private static class HuntingTrackerConditions implements TrackerConditions {

        @Override
        public boolean shouldTrack(BaseTracker tracker) {
            return Utils.isOnSkyblock();
        }

        @Override
        public boolean shouldRender(BaseTracker tracker) {
            return tracker.hasDataInCurrentMode();
        }
    }
}