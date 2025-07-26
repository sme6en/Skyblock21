package com.skyblock21.tracking;

import com.skyblock21.hud.EditGuiScreen;
import com.skyblock21.hud.EditHudElementScreen;
import com.skyblock21.hud.MultiLineHudElement;
import com.skyblock21.util.Location;
import com.skyblock21.util.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.text.NumberFormat;
import java.util.Locale;

public abstract class TrackerHudElement extends MultiLineHudElement {
    protected final BaseTracker tracker;
    protected final NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);

    public TrackerHudElement(int x, int y, BaseTracker tracker) {
        super(x, y);
        this.tracker = tracker;
        setupHud();
    }

    public TrackerHudElement(int x, int y, Location location, BaseTracker tracker) {
        super(x, y, location);
        this.tracker = tracker;
        setupHud();
    }

    protected abstract void setupHud();

    @Override
    protected void onTick(MinecraftClient client) {
        tracker.tick(client);

        // Always update real data lines (even if empty)
        updateHudLines();
    }

    protected abstract void updateHudLines();

    @Override
    protected void renderElement(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();


        super.renderElement(context);
    }

    /**
     * Helper method to create dummy trackable values for display
     */
    protected <T extends Number> TrackableValue<T> createDummyValue(T total, double hoursActive) {
        TrackableValue<T> dummyValue = new TrackableValue<>(total);
        if (hoursActive > 0) {
            dummyValue.updateRate(total.doubleValue() / hoursActive);
        }
        return dummyValue;
    }

    protected String formatValue(Number value) {
        if (value instanceof Integer || value instanceof Long) {
            return formatter.format(value.longValue());
        } else {
            return String.format("%.1f", value.doubleValue());
        }
    }

    protected String formatRate(double rate) {
        return formatter.format(Math.round(rate));
    }

    protected String formatTime(long millis) {
        return Utils.formatTime(millis);
    }

    protected Text createValueLine(String label, String color, TrackableValue<?> value) {
        if (value == null) {
            return Text.literal(String.format("%s%s: §f0 §8(0/hr)", color, label));
        }
        return Text.literal(String.format("%s%s: §f%s §8(%s/hr)",
                color, label, formatValue(value.getTotal()), formatRate(value.getRatePerHour())));
    }

    protected Text createTimeSinceLine(String label, TrackableValue<?> value) {
        if (value == null) {
            return Text.literal(String.format("§7Last %s: §fNever", label));
        }
        long timeSince = value.getTimeSinceLastReceived();
        String timeStr = timeSince == -1 ? "Never" : formatTime(timeSince) + " ago";
        return Text.literal(String.format("§7Last %s: §f%s", label, timeStr));
    }

    protected Text createActiveTimeLine() {
        long activeTime;

        if (tracker.getDisplayMode() == TrackerDisplayMode.ALL_TIME) {
            activeTime = tracker.getTotalActiveTime();
        } else {
            activeTime = tracker.getCurrentActiveTime();
        }

        return Text.literal(String.format("§dActive Time: §f%s", formatTime(activeTime)));
    }

    protected Text createStatusLine() {
        if (tracker.isAfk()) {
            return Text.literal("§c(AFK)");
        } else if (tracker.isPaused()) {
            return Text.literal("§7(Paused)");
        } else {
            return Text.empty();
        }
    }

    protected Text createDisplayModeLine() {
        TrackerDisplayMode mode = tracker.getDisplayMode();
        String color = mode == TrackerDisplayMode.CURRENT_SESSION ? "§e" : "§b";
        return Text.literal(String.format("§7Mode: %s%s", color, mode.getDisplayName()));
    }

    protected void addDisplayModeToggle() {
        if (!tracker.getSettings().persistData) return;

        addContainerClickableLine("toggle_mode",
                Text.literal("§6[Toggle Mode]"),
                () -> {
                    tracker.toggleDisplayMode();
                    updateHudLines(); // Refresh display immediately
                }
        ).setHoverText(Text.literal("§eSwitch between Current Session and All Time data"));
    }

    protected void addResetButtons() {
        // Always add session reset
        addContainerClickableLine("reset_session",
                Text.literal("§e[Reset Session]"),
                () -> {
                    tracker.resetSession();
                    updateHudLines(); // Refresh immediately after reset
                }
        ).setHoverText(Text.literal("§eReset current session data"));

        // Add all data reset if persistence is enabled
        if (tracker.getSettings().persistData) {
            addContainerClickableLine("reset_all",
                    Text.literal("§c[Reset All Data]"),
                    () -> {
                        tracker.resetAll();
                        updateHudLines(); // Refresh immediately after reset
                    }
            ).setHoverText(Text.literal("§cReset both session and persistent data"));
        }
    }

    protected void addModeDisplayLine() {
        if (!tracker.getSettings().persistData) return;

        addLine("display_mode", createDisplayModeLine(), "stats");
    }

    @Override
    public boolean shouldRenderDummy() {
        MinecraftClient client = MinecraftClient.getInstance();
        boolean inEditMode = client.currentScreen instanceof EditGuiScreen ||
                client.currentScreen instanceof EditHudElementScreen;


        return inEditMode && !tracker.hasDataInCurrentMode();
    }
//
//    @Override
//    public boolean isEnabled() {
//        return tracker.getConditions().shouldRender();
//    }
}