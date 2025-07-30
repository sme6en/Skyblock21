package com.skyblock21.hud.elements;

import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.hud.SortType;
import com.skyblock21.tracking.TrackableValue;
import com.skyblock21.tracking.TrackerHudElement;
import com.skyblock21.tracking.impl.BonusGiftsTracker;
import com.skyblock21.util.Location;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static net.minecraft.text.Text.literal;

public class BonusGiftsTrackerElement extends TrackerHudElement {

    public static BonusGiftsTrackerElement INSTANCE;
    private Set<String> trackedGifts = new HashSet<>();

    public BonusGiftsTrackerElement(int x, int y) {
        super(x, y, Location.GALATEA, new BonusGiftsTracker());

        if (INSTANCE == null) {
            INSTANCE = this;
        }
    }

    @Override
    protected void setupHud() {
        setTitle("§d§lBonus Gifts", 0xFF55FF, 1.2f, true);

        createGroup("gifts", "Bonus Gifts", 0, true);
        setGroupSorting("gifts", SortType.AMOUNT, false);

        createGroup("stats", "Stats", 2, false);
        addLine("active_time", literal("§dActive Time: §f0s"), "stats");
        addModeDisplayLine();
        addConditionalLine("status", Text.literal("§7(Paused)"),
                () -> tracker.isPaused() || tracker.isAfk());

        setupDummyData();

        addDisplayModeToggle();
        addResetButtons();
    }

    protected void setupDummyData() {
        addDummyAmountLine("dummy_first_impression", "§dFirst Impression I", 3, "gifts");
        addDummyAmountLine("dummy_stretching_sticks", "§aStretching Sticks", 6, "gifts");
        addDummyAmountLine("dummy_chameleon", "§6Chameleon", 1, "gifts");

        recalculateDimensions();
    }

    @Override
    protected void updateHudLines() {


        updateLine("active_time", createActiveTimeLine());
        updateLine("status", createStatusLine());

        if (tracker.getSettings().persistData) {
            updateLine("display_mode", createDisplayModeLine());
        }

        updateRealGiftLines();
    }

    private void updateRealGiftLines() {
        Map<String, TrackableValue<?>> allValues = tracker.getAllValues();

        lines.removeIf(line ->
                line.getGroupId() != null &&
                        line.getGroupId().equals("gifts") &&
                        line.getId().startsWith("real_gift_") &&
                        !allValues.containsKey(line.getId().replace("real_gift_", ""))
        );

        for (Map.Entry<String, TrackableValue<?>> entry : allValues.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("gift_")) {
                String shardName = key.substring(5);
                String lineId = "real_gift_" + key;

                int shardValue = entry.getValue().asInt();

                if (getLine(lineId) == null) {
                    addAmountLine(lineId, shardName, shardValue, "gifts");
                } else {
                    updateAmountLine(lineId, shardName, shardValue);
                }
            }
        }

        recalculateDimensions();
    }


    @Override
    public boolean shouldRenderDummy() {
        return super.shouldRenderDummy();
    }
}