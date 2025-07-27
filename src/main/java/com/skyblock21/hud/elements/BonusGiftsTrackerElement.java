package com.skyblock21.hud.elements;

import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.hud.SortType;
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
        setTitle("§l§dBonus Gifts", 0xFF55FF, 1.2f, true);

        // Create group for bonus gifts with amount alignment and sorting by amount (descending)
        createGroup("gifts", "Bonus Gifts", 0, true);
        setGroupSorting("gifts", SortType.AMOUNT, false);

        createGroup("stats", "Stats", 2, false);
        addLine("active_time", literal("§dActive Time: §f0s"), "stats");
        addModeDisplayLine();
        addConditionalLine("status", Text.literal("§7(Paused)"),
                () -> tracker.isPaused() || tracker.isAfk());

        // Add dummy data for edit mode
        setupDummyData();

        addDisplayModeToggle();
        addResetButtons();
    }

    protected void setupDummyData() {
        // Add dummy gift data for edit mode
        addDummyAmountLine("dummy_first_impression", "§dFirst Impression I", 3, "gifts");
        addDummyAmountLine("dummy_stretching_sticks", "§aStretching Sticks", 6, "gifts");
        addDummyAmountLine("dummy_chameleon", "§6Chameleon", 1, "gifts");
        addDummyAmountLine("dummy_efficiency", "§9Efficiency VI", 2, "gifts");
        addDummyAmountLine("dummy_looting", "§bLooting IV", 1, "gifts");

        recalculateDimensions();
    }

    @Override
    protected void updateHudLines() {
        // Remove outdated gift lines but keep dummy lines for edit mode
        removeDynamicGiftLines();

        // Update with current gift data
        updateRealGiftLines();

        updateLine("active_time", createActiveTimeLine());
        updateLine("status", createStatusLine());

        if (tracker.getSettings().persistData) {
            updateLine("display_mode", createDisplayModeLine());
        }
    }

    private void removeDynamicGiftLines() {
        Map<String, Integer> bonusDrops = PersistentData.get().bonusDrops;

        // Remove lines for gifts that no longer exist
        lines.removeIf(line ->
                line.getGroupId() != null &&
                        line.getGroupId().equals("gifts") &&
                        line.getId().startsWith("real_gift_") &&
                        !bonusDrops.containsKey(extractGiftNameFromLineId(line.getId()))
        );
    }

    private void updateRealGiftLines() {
        Map<String, Integer> bonusDrops = PersistentData.get().bonusDrops;

        // Add or update gift lines
        for (Map.Entry<String, Integer> entry : bonusDrops.entrySet()) {
            String giftName = entry.getKey();
            int amount = entry.getValue();
            String lineId = "real_gift_" + giftName;

            if (getLine(lineId) == null) {
                addAmountLine(lineId, "§d" + giftName, amount, "gifts");
                trackedGifts.add(giftName);
            } else {
                updateAmountLine(lineId, "§d" + giftName, amount);
            }
        }

        // Remove tracked gifts that no longer exist
        Set<String> toRemove = new HashSet<>();
        for (String giftName : trackedGifts) {
            if (!bonusDrops.containsKey(giftName)) {
                String lineId = "real_gift_" + giftName;
                removeLine(lineId);
                toRemove.add(giftName);
            }
        }
        trackedGifts.removeAll(toRemove);

        if (!toRemove.isEmpty()) {
            recalculateDimensions();
        }
    }

    private void clearGiftLines() {
        // Remove all gift lines (keep control lines)
        lines.removeIf(line ->
                line.getGroupId() != null && line.getGroupId().equals("gifts"));
        trackedGifts.clear();
        recalculateDimensions();
    }

    private String extractGiftNameFromLineId(String lineId) {
        // Extract gift name from line ID by looking up in persistent data
        String hashCode = lineId.replace("real_gift_", "");
        Map<String, Integer> bonusDrops = PersistentData.get().bonusDrops;

        for (String giftName : bonusDrops.keySet()) {
            if (String.valueOf(giftName).equals(hashCode)) {
                return giftName;
            }
        }
        return "";
    }

    public void addOrUpdateGift(String giftName, int amount) {
        String lineId = "real_gift_" + giftName;

        if (trackedGifts.contains(giftName)) {
            updateAmountLine(lineId, "§d" + giftName, amount);
        } else {
            addAmountLine(lineId, "§d" + giftName, amount, "gifts");
            trackedGifts.add(giftName);
        }
    }

    @Override
    public boolean shouldRenderDummy() {
        return super.shouldRenderDummy() || PersistentData.get().bonusDrops.isEmpty();
    }
}