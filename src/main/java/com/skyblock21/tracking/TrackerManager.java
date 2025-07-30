package com.skyblock21.tracking;

import java.util.*;

public class TrackerManager {
    private static final Set<BaseTracker> trackers = new HashSet<>();

    public static void register(BaseTracker tracker) {
        trackers.add(tracker);
    }

    public static void unregister(BaseTracker tracker) {
        trackers.remove(tracker);
    }

    public static Set<BaseTracker> getAllTrackers() {
        return new HashSet<>(trackers);
    }

    public static Optional<BaseTracker> getTrackerById(String id) {
        return trackers.stream()
                .filter(tracker -> tracker.getTrackerId().equals(id))
                .findFirst();
    }

    public static void saveAllTrackers() {
        trackers.forEach(BaseTracker::saveAndClose);
    }

    // Call this from your existing shutdown hook
    public static void shutdown() {
        saveAllTrackers();
    }
}