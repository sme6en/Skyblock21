package com.skyblock21.tracking;

public interface TrackerConditions {
    boolean shouldTrack(BaseTracker tracker);
    boolean shouldRender(BaseTracker tracker);
}