package com.skyblock21.tracking;

import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.util.Location;
import com.skyblock21.util.Utils;
import lombok.Getter;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public abstract class BaseTracker {
    protected final Map<String, TrackableValue<?>> trackedValues = new ConcurrentHashMap<>();
    @Getter
    protected final TrackerConditions conditions;
    @Getter
    protected final TrackerSettings settings;
    @Getter
    protected final String trackerId;

    /**
     * -- GETTER --
     *  Get current display mode
     */
    // Display mode
    @Getter
    protected TrackerDisplayMode displayMode = TrackerDisplayMode.CURRENT_SESSION;

    // Time management
    protected long sessionStartTime = -1;
    protected long lastActionTime = -1;
    protected long totalActiveTime = 0;
    protected long lastActiveTime = -1;
    @Getter
    protected boolean isPaused = true;
    @Getter
    protected boolean isAfk = false;

    // AFK detection
    protected Vec3d lastPlayerPos = null;
    protected float lastPlayerYaw = 0;
    protected float lastPlayerPitch = 0;
    protected long lastMovementTime = -1;

    // State tracking
    protected Location lastLocation = Location.UNKNOWN;

    public BaseTracker(String trackerId, TrackerConditions conditions, TrackerSettings settings) {
        this.trackerId = trackerId;
        this.conditions = conditions;
        this.settings = settings;

        TrackerManager.register(this);

        // Load persistent data if enabled
        if (settings.persistData) {
            loadPersistentData();
        }
    }

    /**
     * Called every tick to update tracker state
     */
    public final void tick(MinecraftClient client) {
        if (!conditions.shouldTrack(this)) {
            if (!isPaused) pauseTracker();
            return;
        }

        // Check for location changes
        Location currentLocation = Utils.getLocation();
        if (!currentLocation.equals(lastLocation)) {
            onLocationChange(lastLocation, currentLocation);
            lastLocation = currentLocation;
        }

        // Update AFK status
        updateAFKStatus(client);

        // Custom tick logic
        onTick(client);

        // Don't update rates every tick - only when values are tracked or mode changes
    }

    /**
     * Track a value increment
     */
    protected <T extends Number> void trackValue(String key, T increment) {
        if (!conditions.shouldTrack(this) || isPaused) return;

        TrackableValue<T> value = getOrCreateValue(key, increment);
        value.add(increment);

        if (settings.persistData) {
            updatePersistentValue(key, increment);
        }

        if (sessionStartTime == -1) {
            startSession();
        }

        lastActionTime = System.currentTimeMillis();
        if (isAfk) exitAfk();

        updateRates();

        onValueTracked(key, increment, value);
    }

    /**
     * Get a tracked value (respects display mode)
     */
    @SuppressWarnings("unchecked")
    public <T extends Number> TrackableValue<T> getValue(String key) {
        if (displayMode == TrackerDisplayMode.ALL_TIME && settings.persistData) {
            return getPersistentValue(key);
        }
        return (TrackableValue<T>) trackedValues.get(key);
    }

    /**
     * Get current session value (ignores display mode)
     */
    @SuppressWarnings("unchecked")
    public <T extends Number> TrackableValue<T> getSessionValue(String key) {
        return (TrackableValue<T>) trackedValues.get(key);
    }

    /**
     * Get persistent value (ignores display mode)
     */
    @SuppressWarnings("unchecked")
    public <T extends Number> TrackableValue<T> getPersistentValue(String key) {
        if (!settings.persistData) return null;

        TrackerData persistentData = PersistentData.get().getTrackerData(trackerId);
        if (persistentData == null) return null;

        return (TrackableValue<T>) persistentData.values.get(key);
    }

    /**
     * Get all tracked values (respects display mode)
     */
    public Map<String, TrackableValue<?>> getAllValues() {
        if (displayMode == TrackerDisplayMode.ALL_TIME && settings.persistData) {
            TrackerData persistentData = PersistentData.get().getTrackerData(trackerId);
            return persistentData != null ? new HashMap<>(persistentData.values) : new HashMap<>();
        }
        return new HashMap<>(trackedValues);
    }

    /**
     * Toggle display mode between current session and all time
     */
    public void toggleDisplayMode() {
        displayMode = displayMode.toggle();
        updateRates();
    }

    public void resetSession() {
        if (settings.persistData && sessionStartTime != -1) {
            long currentSessionActiveTime = getCurrentActiveTime();
            if (currentSessionActiveTime > 0) {
                TrackerData persistentData = PersistentData.get().getOrCreateTrackerData(trackerId);
                persistentData.setTotalActiveTime(getTotalActiveTime());
                PersistentData.save();
            }
        }

        trackedValues.clear();
        sessionStartTime = -1;
        lastActionTime = -1;
        totalActiveTime = 0;
        lastActiveTime = -1;
        isPaused = true;
        isAfk = false;
        lastMovementTime = -1;

        onSessionReset();
    }

    /**
     * Reset all data (session + persistent)
     */
    public void resetAll() {
        resetSession();

        if (settings.persistData) {
            PersistentData.get().clearTrackerData(trackerId);
            PersistentData.save();
        }

        onAllDataReset();
    }

    /**
     * Get current active time in milliseconds (session only)
     */
    public long getCurrentActiveTime() {
        if (sessionStartTime == -1) return 0;

        long currentActive = totalActiveTime;
        if (!isPaused) {
            currentActive += System.currentTimeMillis() - lastActiveTime;
        }
        return currentActive;
    }

    /**
     * Get total active time including persistent data
     */
    public long getTotalActiveTime() {
        long currentActive = getCurrentActiveTime();

        if (settings.persistData) {
            TrackerData persistentData = PersistentData.get().getTrackerData(trackerId);
            if (persistentData != null) {
                currentActive += persistentData.totalActiveTime;
            }
        }

        return currentActive;
    }

    /**
     * Get session duration in milliseconds
     */
    public long getSessionDuration() {
        if (sessionStartTime == -1) return 0;
        return System.currentTimeMillis() - sessionStartTime;
    }

    // ==================== Protected Methods ====================

    @SuppressWarnings("unchecked")
    protected <T extends Number> TrackableValue<T> getOrCreateValue(String key, T sample) {
        return (TrackableValue<T>) trackedValues.computeIfAbsent(key, k -> new TrackableValue<>(sample));
    }

    protected <T extends Number> void updatePersistentValue(String key, T increment) {
        TrackerData persistentData = PersistentData.get().getOrCreateTrackerData(trackerId);

        @SuppressWarnings("unchecked")
        TrackableValue<T> persistentValue = (TrackableValue<T>) persistentData.values.computeIfAbsent(key,
                k -> new TrackableValue<>(increment));

        persistentValue.add(increment);
        PersistentData.save();
    }

    protected void loadPersistentData() {
        TrackerData persistentData = PersistentData.get().getTrackerData(trackerId);
        if (persistentData != null) {
            // Update rates for persistent values
            double hoursActive = persistentData.totalActiveTime / (1000.0 * 60.0 * 60.0);
            if (hoursActive > 0) {
                for (TrackableValue<?> value : persistentData.values.values()) {
                    value.updateRate(hoursActive);
                }
            }
        }
    }

    protected void savePersistentActiveTime() {
        if (!settings.persistData) return;

        TrackerData persistentData = PersistentData.get().getOrCreateTrackerData(trackerId);
        persistentData.totalActiveTime += getCurrentActiveTime();
        PersistentData.save();
    }

    protected void startSession() {
        long currentTime = System.currentTimeMillis();
        sessionStartTime = currentTime;
        lastActiveTime = currentTime;
        lastActionTime = currentTime;
        lastMovementTime = currentTime;
        totalActiveTime = 0;
        isPaused = false;
        isAfk = false;
    }

    protected void pauseTracker() {
        if (!isPaused && sessionStartTime != -1) {
            totalActiveTime += System.currentTimeMillis() - lastActiveTime;
            isPaused = true;
        }
    }

    protected void resumeTracker() {
        if (isPaused && conditions.shouldTrack(this)) {
            lastActiveTime = System.currentTimeMillis();
            isPaused = false;
        }
    }

    protected void updateAFKStatus(MinecraftClient client) {
        if (client.player == null) return;

        Vec3d currentPos = client.player.getPos();
        float currentYaw = client.player.getYaw();
        float currentPitch = client.player.getPitch();

        boolean hasMovedPosition = lastPlayerPos == null || !currentPos.equals(lastPlayerPos);
        boolean hasMovedLook = Math.abs(currentYaw - lastPlayerYaw) > settings.lookSensitivity ||
                Math.abs(currentPitch - lastPlayerPitch) > settings.lookSensitivity;

        if (hasMovedPosition || hasMovedLook) {
            if (isAfk) exitAfk();
            lastMovementTime = System.currentTimeMillis();
            lastPlayerPos = currentPos;
            lastPlayerYaw = currentYaw;
            lastPlayerPitch = currentPitch;
        } else {
            long timeSinceMovement = System.currentTimeMillis() - lastMovementTime;
            if (timeSinceMovement >= settings.afkThreshold && !isAfk) {
                enterAfk();
            }
        }
    }

    protected void enterAfk() {
        isAfk = true;
        pauseTracker();
        onAfkEnter();
    }

    protected void exitAfk() {
        isAfk = false;
        resumeTracker();
        onAfkExit();
    }

    protected void updateRates() {
        long activeTime;
        Map<String, TrackableValue<?>> values;

        if (displayMode == TrackerDisplayMode.ALL_TIME) {
            activeTime = getTotalActiveTime();
            values = getAllValues();
        } else {
            activeTime = getCurrentActiveTime();
            values = trackedValues;
        }

        if (activeTime <= 0) return;

        double hoursActive = activeTime / (1000.0 * 60.0 * 60.0);

        for (TrackableValue<?> value : values.values()) {
            value.updateRate(hoursActive);
        }
    }

    public void saveAndClose() {
        if (settings.persistData && sessionStartTime != -1) {
            long currentSessionActiveTime = getCurrentActiveTime();
            if (currentSessionActiveTime > 0) {
                TrackerData persistentData = PersistentData.get().getOrCreateTrackerData(trackerId);
                persistentData.setTotalActiveTime(getTotalActiveTime());
                PersistentData.save();
            }
        }
    }

    protected void onLocationChange(Location from, Location to) {
        if (settings.pauseOnLocationChange &&
                (settings.allowedLocations.length > 0 && !Arrays.asList(settings.allowedLocations).contains(to))) {
            pauseTracker();
        } else if (Arrays.asList(settings.allowedLocations).contains(to)) {
            resumeTracker();
        }
    }

    // ==================== Abstract/Override Methods ====================

    protected abstract void onTick(MinecraftClient client);
    protected abstract void onValueTracked(String key, Number increment, TrackableValue<?> value);

    protected void onSessionReset() {}
    protected void onAllDataReset() {}
    protected void onAfkEnter() {}
    protected void onAfkExit() {}

    /**
     * Check if tracker has any meaningful data to display
     */
    public boolean hasData() {
        if (trackedValues.isEmpty()) return false;

        // Check if any values have been tracked
        return trackedValues.values().stream()
                            .anyMatch(value -> value.getTotal().doubleValue() > 0 || value.getTimesReceived() > 0);
    }

    public boolean hasDataInCurrentMode() {
        Map<String, TrackableValue<?>> values = getAllValues();
        if (values.isEmpty()) return false;

        return values.values().stream()
                     .anyMatch(value -> value.getTotal().doubleValue() > 0 || value.getTimesReceived() > 0);
    }
}