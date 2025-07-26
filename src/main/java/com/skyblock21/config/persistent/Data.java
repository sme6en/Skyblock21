package com.skyblock21.config.persistent;

import com.skyblock21.features.itemcustomization.ItemCustomization;
import com.skyblock21.features.Scathas.ScathasData;
import com.skyblock21.features.commandaliases.Alias;
import com.skyblock21.features.keyshortcuts.Shortcut;
import com.skyblock21.tracking.TrackerData;
import dev.isxander.yacl3.config.v2.api.SerialEntry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Data {

    @SerialEntry
    public ScathasData scathasData = new ScathasData();

    @SerialEntry
    public Map<String, Integer> bonusDrops = new HashMap<>();

    @SerialEntry
    public final Set<Shortcut> shortcuts = new HashSet<>();

    @SerialEntry
    public final Set<Alias> aliases = new HashSet<>();

    @SerialEntry
    public Map<String, ItemCustomization> itemCustomizations = new HashMap<>();

    @SerialEntry
    public Map<String, TrackerData> trackerData = new HashMap<>();

    public TrackerData getTrackerData(String trackerId) {
        return trackerData.get(trackerId);
    }

    public TrackerData getOrCreateTrackerData(String trackerId) {
        return trackerData.computeIfAbsent(trackerId, k -> new TrackerData());
    }

    public void clearTrackerData(String trackerId) {
        trackerData.remove(trackerId);
    }

    public void clearAllTrackerData() {
        trackerData.clear();
    }
}
