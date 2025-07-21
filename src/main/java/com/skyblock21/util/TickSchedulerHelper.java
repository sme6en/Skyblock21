package com.skyblock21.util;

public class TickSchedulerHelper {

    /**
     * Get the scheduler instance
     */
    public static TickScheduler getScheduler() {
        return TickScheduler.getInstance();
    }

    // ===== Tick-based methods =====

    /**
     * Schedule a delayed action in ticks
     */
    public static String delay(Runnable action, long ticks) {
        return getScheduler().scheduleTask(action, ticks);
    }

    /**
     * Schedule a repeating action every N ticks
     */
    public static String repeat(Runnable action, long intervalTicks) {
        return getScheduler().scheduleRepeatingTask(action, 0, intervalTicks);
    }

    /**
     * Schedule a repeating action with initial delay in ticks
     */
    public static String repeatWithDelay(Runnable action, long delayTicks, long intervalTicks) {
        return getScheduler().scheduleRepeatingTask(action, delayTicks, intervalTicks);
    }

    /**
     * Run action N times with interval in ticks
     */
    public static String runNTimes(Runnable action, int times, long intervalTicks) {
        return getScheduler().scheduleLimitedTask("limited_" + System.nanoTime(), action, intervalTicks, times);
    }

    // ===== Second-based methods (converted to ticks) =====

    /**
     * Schedule a delayed action in seconds
     */
    public static String delaySeconds(Runnable action, double seconds) {
        return getScheduler().scheduleTaskInSeconds(action, seconds);
    }

    /**
     * Schedule action to run every second (20 ticks)
     */
    public static String everySecond(Runnable action) {
        return getScheduler().scheduleRepeatingTask(action, 0, 20);
    }

    /**
     * Schedule action to run every N seconds
     */
    public static String everySeconds(Runnable action, double seconds) {
        return getScheduler().scheduleRepeatingTaskInSeconds(action, 0, seconds);
    }

    /**
     * Schedule action to run every N seconds with initial delay
     */
    public static String everySecondsWithDelay(Runnable action, double delaySeconds, double intervalSeconds) {
        return getScheduler().scheduleRepeatingTaskInSeconds(action, delaySeconds, intervalSeconds);
    }

    /**
     * Run action N times with interval in seconds
     */
    public static String runNTimesSeconds(Runnable action, int times, double intervalSeconds) {
        return getScheduler().scheduleLimitedTaskInSeconds("limited_" + System.nanoTime(), action, intervalSeconds, times);
    }

    // ===== Common patterns =====

    /**
     * Cancel a task by ID
     */
    public static boolean cancel(String taskId) {
        return getScheduler().cancelTask(taskId);
    }

    /**
     * Run action after delay with automatic cleanup (fire and forget)
     */
    public static void runAfter(Runnable action, long ticks) {
        getScheduler().scheduleTask(action, ticks);
    }

    /**
     * Run action after delay in seconds (fire and forget)
     */
    public static void runAfterSeconds(Runnable action, double seconds) {
        getScheduler().scheduleTaskInSeconds(action, seconds);
    }

    /**
     * Debounce: Run action after delay, cancel previous if called again
     */
    private static String lastDebounceTaskId = null;

    public static void debounce(Runnable action, long delayTicks) {
        // Cancel previous debounced task
        if (lastDebounceTaskId != null) {
            getScheduler().cancelTask(lastDebounceTaskId);
        }

        // Schedule new task
        lastDebounceTaskId = getScheduler().scheduleTask(action, delayTicks);
    }

    /**
     * Debounce with delay in seconds
     */
    public static void debounceSeconds(Runnable action, double delaySeconds) {
        // Cancel previous debounced task
        if (lastDebounceTaskId != null) {
            getScheduler().cancelTask(lastDebounceTaskId);
        }

        // Schedule new task
        lastDebounceTaskId = getScheduler().scheduleTaskInSeconds(action, delaySeconds);
    }

    /**
     * Get scheduler info
     */
    public static long getCurrentTick() {
        return getScheduler().getCurrentTick();
    }

    public static int getActiveTaskCount() {
        return getScheduler().getActiveTaskCount();
    }

    public static void cancelAllTasks() {
        getScheduler().cancelAllTasks();
    }
}