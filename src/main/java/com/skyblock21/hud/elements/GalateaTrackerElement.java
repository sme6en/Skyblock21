package com.skyblock21.hud.elements;

import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.tracking.TrackableValue;
import com.skyblock21.tracking.TrackerDisplayMode;
import com.skyblock21.tracking.TrackerHudElement;
import com.skyblock21.tracking.impl.GalateaTracker;
import com.skyblock21.util.Location;
import net.minecraft.text.Text;

public class GalateaTrackerElement extends TrackerHudElement {

    public GalateaTrackerElement(int x, int y) {
        super(x, y, Location.GALATEA, new GalateaTracker());
    }

    @Override
    protected void setupHud() {
        setTitle("§2§lGalatea Tracker", 0x55FF55, 1.2f, true);

        // Main stats group
        createGroup("stats", "Stats", 0, false);
        addLine("forest_essence", Text.literal("§8Forest Essence: §f0 §8(0/hr)"), "stats");
        addLine("foraging_exp", Text.literal("§3Foraging Exp: §f0 §8(0/hr)"), "stats");
        addLine("hotf_exp", Text.literal("§aHOTF Exp: §f0 §8(0/hr)"), "stats");
        addLine("whispers", Text.literal("§2Whispers: §f0 §8(0/hr)"), "stats");

        addLine("active_time", Text.literal("§dActive Time: §f0s"), "stats");
        addModeDisplayLine();
        addConditionalLine("status", Text.literal("§7(Paused)"),
                () -> tracker.isPaused() || tracker.isAfk());

        // Add dummy data for edit mode
        setupDummyData();

        addDisplayModeToggle();
        addResetButtons();
    }

    protected void setupDummyData() {
        TrackableValue<Integer> dummyForestEssence = createDummyValue(900, 1.25);
        TrackableValue<Integer> dummyForagingExp = createDummyValue(25000, 1.25);
        TrackableValue<Integer> dummyHOTFExp = createDummyValue(2500, 1.25);
        TrackableValue<Integer> dummyWhispers = createDummyValue(500, 1.25);

        addDummyLine("dummy_forest_essence", createForestEssenceLine(dummyForestEssence), "stats");
        addDummyLine("dummy_foraging_exp", createValueLine("Foraging Exp", "§3", dummyForagingExp), "stats");
        addDummyLine("dummy_hotf_exp",  createValueLine("HOTF Exp", "§a", dummyHOTFExp), "stats");
        addDummyLine("dummy_whispers", createValueLine("Whispers", "§2", dummyWhispers), "stats");
        addDummyLine("dummy_active_time", Text.literal("§dActive Time: §f1h 23m 45s"), "stats");
        addDummyLine("dummy_status", Text.literal("§c(AFK)"), "stats");

        if (tracker.getSettings().persistData) {
            addDummyLine("dummy_display_mode", Text.literal("§7Mode: §eCurrent Session"), "stats");
        }

        recalculateDimensions();
    }

    @Override
    protected void updateHudLines() {
        // Get values from tracker
        TrackableValue<Integer> forestEssence = tracker.getValue("forest_essence");
        TrackableValue<Integer> foragingExp = tracker.getValue("foraging_exp");
        TrackableValue<Integer> hotfExp = tracker.getValue("hotf_exp");
        TrackableValue<Integer> whispers = tracker.getValue("whispers");

        // Update lines with current values
        updateLine("forest_essence", createForestEssenceLine(forestEssence));
        updateLine("foraging_exp", createValueLine("Foraging Exp", "§3", foragingExp));
        updateLine("hotf_exp", createValueLine("HOTF Exp", "§a", hotfExp));
        updateLine("whispers", createValueLine("Whispers", "§2", whispers));
        updateLine("active_time", createActiveTimeLine());
        updateLine("status", createStatusLine());

        if (tracker.getSettings().persistData) {
            updateLine("display_mode", createDisplayModeLine());
        }
    }

    private Text createForestEssenceLine(TrackableValue<Integer> value) {
        if (value == null) {
            return Text.literal("§8Forest Essence: §f0");
        }
        return createValueLine("Forest Essence", "§8", value);
//        return Text.literal(String.format("§8Forest Essence: §f%s", formatValue(value.getTotal())));
    }

    @Override
    public boolean isEnabled() {
        return Skyblock21ConfigManager.get().foraging.galateaTracker;
    }
}