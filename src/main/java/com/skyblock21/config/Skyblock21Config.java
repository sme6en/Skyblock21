package com.skyblock21.config;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class Skyblock21Config {

    @SerialEntry
    public General general = new General();

    @SerialEntry
    public Mining mining = new Mining();

    @SerialEntry
    public Foraging foraging = new Foraging();

    @SerialEntry
    public Nether nether = new Nether();

    public static class General {
        @SerialEntry
        public boolean noFog = false;
        @SerialEntry
        public boolean timestampBeforeMessages = false;
        @SerialEntry
        public boolean copyToClipboardRNGs = true;

        @SerialEntry
        public boolean preventDirtRoads = true;

        @SerialEntry
        public boolean hidePlayersAroundNpcs = true;

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
        public boolean showOnlyInCrystalHollows = false;
        @SerialEntry
        public boolean mouseLockKeybind = true;
    }

    public static class Foraging {
        @SerialEntry
        public boolean galateaTracker = true;
        @SerialEntry
        public boolean bonusGiftsTracker = true;
        @SerialEntry
        public int afkTimeout = 30; // in seconds

        @SerialEntry
        public boolean treeWaypoints = true;
        @SerialEntry
        public boolean onlyShowSmallTrees = false;
        @SerialEntry
        public boolean showFigTreeWaypoints = true;
        @SerialEntry
        public boolean showMangroveTreeWaypoints = true;
        @SerialEntry
        public int timeBeforeReady = 5;
        @SerialEntry
        public int maxDistance = 0;

        @SerialEntry
        public boolean preventLogStripping = true;

        @SerialEntry
        public boolean hideFloatingBlocks = true;
    }

    public static class Nether {
        @SerialEntry
        public boolean kuudraSupplyHelper = true;
    }
}
