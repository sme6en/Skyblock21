package com.skyblock21.config;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class Skyblock21Config {

    @SerialEntry
    public General general = new General();

    @SerialEntry
    public Mining mining = new Mining();

    @SerialEntry
    public Foraging foraging = new Foraging();

    public static class General {
        @SerialEntry
        public boolean noFog = false;
        @SerialEntry
        public boolean timestampBeforeMessages = false;
        @SerialEntry
        public boolean copyToClipboardRNGs = true;
        @SerialEntry
        public boolean boosterCookieReminder = true;
        @SerialEntry
        public int boosterCookieReminderHours = 6;
        @SerialEntry
        public boolean godPotReminder = true;

        @SerialEntry
        public boolean preventDroppingStarredItems = true;

        @SerialEntry
        public CompactStarMode compactStarMode = CompactStarMode.NONE;

        public enum CompactStarMode {
            NONE,
            COMPACT,
            COMPACT_TILL_TEN // Compact items with their count
        }
    }

    public static class Mining {
        @SerialEntry
        public boolean scathaAlerts = true;
        @SerialEntry
        public boolean scathaTracker = true;
        @SerialEntry
        public boolean mouseLockKeybind = true;
    }

    public static class Foraging {
        @SerialEntry
        public boolean galateaTracker = true;
        @SerialEntry
        public int afkTimeout = 30; // in seconds
    }
}
