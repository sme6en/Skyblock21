package com.skyblock21.hud.elements;

import com.skyblock21.hud.SortType;
import com.skyblock21.tracking.TrackableValue;
import com.skyblock21.tracking.TrackerDisplayMode;
import com.skyblock21.tracking.TrackerHudElement;
import com.skyblock21.tracking.impl.HuntingTracker;
import net.minecraft.text.Text;

import java.util.*;

import static net.minecraft.text.Text.literal;

public class HuntingTrackerElement extends TrackerHudElement {


    public HuntingTrackerElement(int x, int y) {
        super(x, y, new HuntingTracker());
    }

    @Override
    protected void setupHud() {
        setTitle("§9§lHunting", 0xFFFFFF, 1.2f, true);


        createGroup("stats", "Stats", 2, false);
        addLine("hunting_exp", literal("§9Hunting XP: §f0 §8(0/hr)"), "stats");
        addLine("active_time", literal("§dActive Time: §f0s"), "stats");
        addModeDisplayLine();

        addConditionalLine("status", literal("§7(Paused)"),
                () -> tracker.isPaused() || tracker.isAfk());

        addSpacer("shard_spacer", 1);

        createGroup("shards", "Shards", 1, true);
        setGroupSorting("shards", SortType.AMOUNT, false);

        addDummyAmountLine("dummy_salt", "§dSalt", 5, "shards");
        addDummyAmountLine("dummy_charm", "§5Charm", 3, "shards");
        addDummyAmountLine("dummy_naga", "§6Naga", 1, "shards");
        addDummyAmountLine("dummy_naga", "§9Glacite Walker", 3, "shards");
        addDummySpacer("dummy_spacer", 1);
        addDummyLine("dummy_hunting_exp", literal("§9Hunting XP: §f0 §8(0/hr)"), "stats");
        addDummyLine("dummy_active_time", literal("§dActive Time: §f0s"), "stats");

        addDisplayModeToggle();
        addResetButtons();
    }

    @Override
    protected void updateHudLines() {

        TrackableValue<Integer> huntingExp = tracker.getValue("hunting_exp");

        updateLine("hunting_exp", createValueLine("Hunting XP", "§9", huntingExp));
        updateLine("active_time", createActiveTimeLine());
        updateLine("status", createStatusLine());

        if (tracker.getSettings().persistData) {
            updateLine("display_mode", createDisplayModeLine());
        }

        updateRealShardLines();
    }

    private void updateRealShardLines() {
        Map<String, TrackableValue<?>> allValues = tracker.getAllValues();

        lines.removeIf(line ->
                line.getGroupId() != null &&
                        line.getGroupId().equals("shards") &&
                        line.getId().startsWith("real_shard_") &&
                        !allValues.containsKey(line.getId().replace("real_shard_", ""))
        );

        for (Map.Entry<String, TrackableValue<?>> entry : allValues.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("shard_")) {
                String shardName = key.substring(6);
                String lineId = "real_shard_" + key;

                int shardValue = entry.getValue().asInt();

                if (getLine(lineId) == null) {
                    addAmountLine(lineId, "§f" + shardName, shardValue, "shards");
                } else {
                    updateAmountLine(lineId, "§f" + shardName, shardValue);
                }
            }
        }

        recalculateDimensions();
    }
}