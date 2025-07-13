package com.skyblock21.hud.elements;

import com.skyblock21.config.Skyblock21Config;
import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.events.ChatEvents;
import com.skyblock21.events.SkyblockEvents;
import com.skyblock21.features.foraging.GalateaTracker;
import com.skyblock21.hud.MultiLineHudElement;
import com.skyblock21.hud.SortType;
import com.skyblock21.util.TextUtils;
import com.skyblock21.util.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.text.Text.literal;

public class HuntingTrackerElement extends MultiLineHudElement {

    private Map<String, Integer> trackedShards;
    private int totalHuntingXP = 0;
    private long activeTimeStart = System.currentTimeMillis();

    public HuntingTrackerElement(int x, int y) {
        super(x, y);

        setTitle("§d§lHunting", 0xFFFFFF, 1.2f, true);

        addLine("hunting_exp", literal("§9Hunting XP: §f%d".formatted(totalHuntingXP)));
        addLine("active_time", literal("§dActive Time: §f0s"));
        // Create group for bonus gifts with amount alignment and sorting by amount (descending)
        createGroup("shards", "Shards", 1, true);
        setGroupSorting("shards", SortType.AMOUNT, false);

        // Add control lines (only show in containers)
        addContainerClickableLine("reset_hunting",
                literal("§c[Reset Tracker]"),
                this::resetTracker
        ).setHoverText(literal("§eReset the hunting tracker"));

        SkyblockEvents.SKILL_GAINED.register(this::onSkillGained);
        ChatEvents.RECEIVE_TEXT.register(this::onChat);
        trackedShards = new HashMap<String, Integer>();
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
        if (activeTimeStart == -1) return;

        updateLine("active_time", literal("§dActive Time: §f%s".formatted(Utils.formatTime(System.currentTimeMillis() - activeTimeStart))));
    }
    private static final NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);

    private void onSkillGained(SkyblockEvents.Skill skill, double amount) {
        if (skill != SkyblockEvents.Skill.HUNTING) return;

        if (totalHuntingXP == 0) {
            activeTimeStart = System.currentTimeMillis();
        }

        totalHuntingXP += (int) amount;
        updateLine("hunting_exp", literal("§9Hunting XP: §f%s".formatted(formatter.format(totalHuntingXP))));
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
        lines.removeIf(line ->
                line.getGroupId() != null && line.getGroupId().equals("shards"));
        TextUtils.addMessage("§aHunting tracker reset!", true, false);
        updateLine("hunting_exp", literal("§9Hunting XP: §f%d".formatted(totalHuntingXP)));
        updateLine("active_time", literal("§dActive Time: §f0s"));
        activeTimeStart = -1;
    }

    @Override
    public boolean isEnabled() {
        return Skyblock21ConfigManager.get().hunting.huntingTracker;
    }
}
