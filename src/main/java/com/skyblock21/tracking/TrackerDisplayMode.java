package com.skyblock21.tracking;

public enum TrackerDisplayMode {
    CURRENT_SESSION("Session"),
    ALL_TIME("All Time");

    private final String displayName;

    TrackerDisplayMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public TrackerDisplayMode toggle() {
        return this == CURRENT_SESSION ? ALL_TIME : CURRENT_SESSION;
    }
}
