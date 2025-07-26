package com.skyblock21.tracking;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TrackerData {
    public Map<String, TrackableValue<?>> values = new ConcurrentHashMap<>();
    public long totalActiveTime = 0;

    public void setTotalActiveTime(long activeTime) {
        this.totalActiveTime = activeTime;
    }

    public TrackerData() {}
}