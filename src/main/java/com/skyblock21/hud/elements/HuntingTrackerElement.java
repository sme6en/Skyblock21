package com.skyblock21.hud.elements;

import com.skyblock21.config.Skyblock21Config;
import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.events.ChatEvents;
import com.skyblock21.events.SkyblockEvents;
import com.skyblock21.features.foraging.GalateaTracker;
import com.skyblock21.hud.MultiLineHudElement;
import com.skyblock21.hud.SortType;
import com.skyblock21.util.Location;
import com.skyblock21.util.TextUtils;
import com.skyblock21.util.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.text.Text.literal;

public class HuntingTrackerElement extends MultiLineHudElement {

    private Map<String, Integer> trackedShards;
    private int totalHuntingXP = 0;
    private long activeTimeStart = System.currentTimeMillis();
    private long totalActiveTime = 0;
    private long lastActiveTime = System.currentTimeMillis();

    // AFK detection
    private boolean isAFK = false;
    private Vec3d lastPlayerPos = null;
    private float lastPlayerYaw = 0;
    private float lastPlayerPitch = 0;
    private long lastMovementTime = System.currentTimeMillis();
    private static final long AFK_THRESHOLD = 10_000;

    private boolean paused = true;

    public HuntingTrackerElement(int x, int y) {
        super(x, y);

        setTitle("§9§lHunting", 0xFFFFFF, 1.2f, true);

        addLine("hunting_exp", literal("§9Hunting XP: §f%d".formatted(totalHuntingXP)));
        addLine("hunting_rate", literal("§9XP/Hour: §f0"));
        addLine("active_time", literal("§dActive Time: §f0s"));
        addConditionalLine("afk_status", literal("§7(Paused)"), () -> isAFK);

        createGroup("shards", "Shards", 1, true);
        setGroupSorting("shards", SortType.AMOUNT, false);

        addContainerClickableLine("reset_hunting",
                literal("§c[Reset Tracker]"),
                this::resetTracker
        ).setHoverText(literal("§eReset the hunting tracker"));

        trackedShards = new HashMap<String, Integer>();
        SkyblockEvents.SKILL_GAINED.register(this::onSkillGained);
        SkyblockEvents.LOCATION_CHANGE.register(this::onLocationChange);
        ChatEvents.RECEIVE_TEXT.register(this::onChat);
    }

    private void onLocationChange(Location location) {
        if (!Utils.isOnSkyblock()) {
            pauseTracker();
        }
    }

    private static final Pattern SHARD_PATTERN = Pattern.compile("(?:a|(\\d+)) (.*?) §.Shard");

    private void onChat(Text text) {
        String message = text.getString();
        Matcher matcher = SHARD_PATTERN.matcher(message);

        if (!matcher.find()) return;

        String amount = matcher.group(1);
        String itemName = matcher.group(2);

        int count = (amount != null) ? Integer.parseInt(amount) : 1;

        addOrUpdateShard(itemName, count);
    }

    @Override
    protected void onTick(MinecraftClient client) {
        if (client.player == null) return;

        updateAFKStatus(client);

        if (activeTimeStart != -1) {
            long currentActiveTime = calculateCurrentActiveTime();
            updateLine("active_time", literal("§dActive Time: §f%s".formatted(Utils.formatTime(currentActiveTime))));
        }
    }

    private void updateAFKStatus(MinecraftClient client) {
        Vec3d currentPos = client.player.getPos();
        float currentYaw = client.player.getYaw();
        float currentPitch = client.player.getPitch();

        boolean hasMovedPosition = lastPlayerPos == null ||
                !currentPos.equals(lastPlayerPos);
        boolean hasMovedLook = Math.abs(currentYaw - lastPlayerYaw) > 0.2f ||
                Math.abs(currentPitch - lastPlayerPitch) > 0.2f;

        if (hasMovedPosition || hasMovedLook) {
            if (isAFK) {
                resumeTracker();
            }
            lastMovementTime = System.currentTimeMillis();
            lastPlayerPos = currentPos;
            lastPlayerYaw = currentYaw;
            lastPlayerPitch = currentPitch;
            isAFK = false;
        } else {
            long timeSinceMovement = System.currentTimeMillis() - lastMovementTime;
            if (timeSinceMovement >= AFK_THRESHOLD && !isAFK) {
                pauseTracker();
                isAFK = true;
            }
        }
    }

    private void pauseTracker() {
        if (!paused && activeTimeStart != -1) {
            totalActiveTime += System.currentTimeMillis() - lastActiveTime;
            paused = true;
        }
    }

    private void resumeTracker() {
        if (paused) {
            lastActiveTime = System.currentTimeMillis();
            paused = false;
        }
    }

    private long calculateCurrentActiveTime() {
        if (activeTimeStart == -1) return 0;

        long currentActive = totalActiveTime;
        if (!paused) {
            currentActive += System.currentTimeMillis() - lastActiveTime;
        }
        return currentActive;
    }

    private void updateRates() {
        long currentActiveTime = calculateCurrentActiveTime();

        if (currentActiveTime > 0) {
            double hoursActive = currentActiveTime / (1000.0 * 60.0 * 60.0);
            long xpPerHour = Math.round(totalHuntingXP / hoursActive);

            updateLine("hunting_rate", literal("§9XP/Hour: §f%s".formatted(formatter.format(xpPerHour))));
        } else {
            updateLine("hunting_rate", literal("§9XP/Hour: §f0"));
        }
    }

    private static final NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);

    private void onSkillGained(SkyblockEvents.Skill skill, double amount) {
        if (skill != SkyblockEvents.Skill.HUNTING) return;

        if (totalHuntingXP == 0) {
            activeTimeStart = System.currentTimeMillis();
            lastActiveTime = System.currentTimeMillis();
            totalActiveTime = 0;
        }

        paused = false;
        isAFK = false;
        lastMovementTime = System.currentTimeMillis();

        totalHuntingXP += (int) amount;
        updateLine("hunting_exp", literal("§9Hunting XP: §f%s".formatted(formatter.format(totalHuntingXP))));

        updateRates();
    }

    public void addOrUpdateShard(String shard, int amount) {
        if (trackedShards.containsKey(shard)) {
            updateAmountLine(shard, shard, trackedShards.get(shard) + amount);
            trackedShards.put(shard, trackedShards.get(shard) + amount);
        } else {
            addAmountLine(shard, shard, amount, "shards");
            trackedShards.put(shard, amount);
        }
    }

    public void resetTracker() {
        trackedShards.clear();
        totalHuntingXP = 0;
        totalActiveTime = 0;
        activeTimeStart = -1;
        lastActiveTime = System.currentTimeMillis();
        paused = true;
        isAFK = false;

        lines.removeIf(line ->
                line.getGroupId() != null && line.getGroupId().equals("shards"));

        TextUtils.addMessage("§aHunting tracker reset!", true, false);

        updateLine("hunting_exp", literal("§9Hunting XP: §f%d".formatted(totalHuntingXP)));
        updateLine("hunting_rate", literal("§9XP/Hour: §f0"));
        updateLine("active_time", literal("§dActive Time: §f0s"));
        updateLine("afk_status", literal("§7Status: §aActive"));
    }

    @Override
    public boolean isEnabled() {
        return Skyblock21ConfigManager.get().hunting.huntingTracker;
    }

    @Override
    public boolean shouldRenderDummy() {
        return trackedShards.isEmpty();
    }
}