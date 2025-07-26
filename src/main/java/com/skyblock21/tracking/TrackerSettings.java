package com.skyblock21.tracking;

import com.skyblock21.util.Location;

public class TrackerSettings {
    public final long afkThreshold;
    public final float lookSensitivity;
    public final boolean pauseOnLocationChange;
    public final Location[] allowedLocations;
    public final boolean persistData;

    public TrackerSettings(long afkThreshold, float lookSensitivity, boolean pauseOnLocationChange,
                           Location[] allowedLocations, boolean persistData) {
        this.afkThreshold = afkThreshold;
        this.lookSensitivity = lookSensitivity;
        this.pauseOnLocationChange = pauseOnLocationChange;
        this.allowedLocations = allowedLocations;
        this.persistData = persistData;
    }

    public static TrackerSettings defaults() {
        return new TrackerSettings(10000, 0.2f, true, new Location[0], false);
    }

    public static TrackerSettings forLocation(Location... locations) {
        return new TrackerSettings(10000, 0.2f, true, locations, false);
    }

    public static TrackerSettings withPersistence(Location... locations) {
        return new TrackerSettings(10000, 0.2f, true, locations, true);
    }
}