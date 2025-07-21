package com.skyblock21.util;

import com.skyblock21.Skyblock21;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TickScheduler {

    private static TickScheduler instance;

    // Task storage
    private final Map<String, ScheduledTask> tasks = new ConcurrentHashMap<>();
    private final AtomicLong taskIdCounter = new AtomicLong(0);

    // Current tick counter
    private long currentTick = 0;

    // Task execution queue (sorted by execution time)
    private final PriorityQueue<ScheduledTask> taskQueue = new PriorityQueue<>(
            Comparator.comparing(ScheduledTask::getNextExecutionTick)
    );

    private TickScheduler() {
    }

    /**
     * Get scheduler instance
     */
    public static TickScheduler getInstance() {
        if (instance == null) {
            instance = new TickScheduler();
            instance.initialize();
        }
        return instance;
    }

    /**
     * Initialize client-side tick events
     */
    private void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null) {
                tick();
            }
        });
    }

    /**
     * Main tick processing method
     */
    private void tick() {
        currentTick++;

        // Process all tasks that are ready to execute
        while (!taskQueue.isEmpty() && taskQueue.peek().getNextExecutionTick() <= currentTick) {
            ScheduledTask task = taskQueue.poll();

            if (task != null && !task.isCancelled()) {
                try {
                    // Execute the task
                    task.execute();

                    // If it's a repeating task, reschedule it
                    if (task.isRepeating() && !task.isCancelled()) {
                        task.scheduleNext(currentTick);
                        taskQueue.offer(task);
                    } else {
                        // Remove one-time tasks
                        tasks.remove(task.getId());
                    }

                } catch (Exception e) {
                    Skyblock21.LOGGER.error("Error executing scheduled task {}: {}", task.getId(), e.getMessage());

                    // Cancel task on error to prevent spam
                    task.cancel();
                    tasks.remove(task.getId());
                }
            }
        }
    }

    /**
     * Schedule a one-time task to run after specified ticks
     */
    public String scheduleTask(Runnable task, long delayTicks) {
        return scheduleTask("auto_" + taskIdCounter.incrementAndGet(), task, delayTicks);
    }

    /**
     * Schedule a one-time task with custom ID
     */
    public String scheduleTask(String taskId, Runnable task, long delayTicks) {
        if (delayTicks < 0) {
            throw new IllegalArgumentException("Delay cannot be negative");
        }

        ScheduledTask scheduledTask = new ScheduledTask(
                taskId,
                task,
                currentTick + delayTicks,
                -1, // No repeat
                false
        );

        tasks.put(taskId, scheduledTask);
        taskQueue.offer(scheduledTask);

        return taskId;
    }

    /**
     * Schedule a repeating task
     */
    public String scheduleRepeatingTask(Runnable task, long delayTicks, long intervalTicks) {
        return scheduleRepeatingTask("auto_" + taskIdCounter.incrementAndGet(), task, delayTicks, intervalTicks);
    }

    /**
     * Schedule a repeating task with custom ID
     */
    public String scheduleRepeatingTask(String taskId, Runnable task, long delayTicks, long intervalTicks) {
        if (delayTicks < 0 || intervalTicks <= 0) {
            throw new IllegalArgumentException("Invalid delay or interval");
        }

        ScheduledTask scheduledTask = new ScheduledTask(
                taskId,
                task,
                currentTick + delayTicks,
                intervalTicks,
                true
        );

        tasks.put(taskId, scheduledTask);
        taskQueue.offer(scheduledTask);

        return taskId;
    }

    /**
     * Schedule a task that runs for a limited number of times
     */
    public String scheduleLimitedTask(String taskId, Runnable task, long intervalTicks, int maxExecutions) {
        final AtomicCounter counter = new AtomicCounter(0);

        Runnable limitedTask = () -> {
            task.run();

            // Check execution limit
            if (counter.incrementAndGet() >= maxExecutions) {
                cancelTask(taskId);
            }
        };

        return scheduleRepeatingTask(taskId, limitedTask, 0, intervalTicks);
    }

    /**
     * Schedule a conditional task that repeats until condition is met
     */
    public String scheduleConditionalTask(String taskId, Runnable task, long intervalTicks,
                                          java.util.function.BooleanSupplier stopCondition) {
        Runnable conditionalTask = () -> {
            task.run();

            // Check stop condition after execution
            if (stopCondition.getAsBoolean()) {
                cancelTask(taskId);
            }
        };

        return scheduleRepeatingTask(taskId, conditionalTask, 0, intervalTicks);
    }

    /**
     * Cancel a scheduled task
     */
    public boolean cancelTask(String taskId) {
        ScheduledTask task = tasks.remove(taskId);
        if (task != null) {
            task.cancel();
            return true;
        }
        return false;
    }

    /**
     * Check if a task exists and is not cancelled
     */
    public boolean hasTask(String taskId) {
        ScheduledTask task = tasks.get(taskId);
        return task != null && !task.isCancelled();
    }

    /**
     * Cancel all tasks
     */
    public void cancelAllTasks() {
        for (ScheduledTask task : tasks.values()) {
            task.cancel();
        }
        tasks.clear();
        taskQueue.clear();
    }

    /**
     * Get current tick count
     */
    public long getCurrentTick() {
        return currentTick;
    }

    /**
     * Get all active task IDs
     */
    public Set<String> getActiveTaskIds() {
        return new HashSet<>(tasks.keySet());
    }

    /**
     * Get number of active tasks
     */
    public int getActiveTaskCount() {
        return tasks.size();
    }

    // ===== Time-based convenience methods =====

    /**
     * Schedule task to run after specified seconds (converted to ticks)
     */
    public String scheduleTaskInSeconds(Runnable task, double seconds) {
        return scheduleTask(task, secondsToTicks(seconds));
    }

    /**
     * Schedule task to run after specified seconds with custom ID
     */
    public String scheduleTaskInSeconds(String taskId, Runnable task, double seconds) {
        return scheduleTask(taskId, task, secondsToTicks(seconds));
    }

    /**
     * Schedule repeating task with delays/intervals in seconds
     */
    public String scheduleRepeatingTaskInSeconds(Runnable task, double delaySeconds, double intervalSeconds) {
        return scheduleRepeatingTask(task, secondsToTicks(delaySeconds), secondsToTicks(intervalSeconds));
    }

    /**
     * Schedule repeating task with delays/intervals in seconds and custom ID
     */
    public String scheduleRepeatingTaskInSeconds(String taskId, Runnable task, double delaySeconds, double intervalSeconds) {
        return scheduleRepeatingTask(taskId, task, secondsToTicks(delaySeconds), secondsToTicks(intervalSeconds));
    }

    /**
     * Schedule limited task with interval in seconds
     */
    public String scheduleLimitedTaskInSeconds(String taskId, Runnable task, double intervalSeconds, int maxExecutions) {
        return scheduleLimitedTask(taskId, task, secondsToTicks(intervalSeconds), maxExecutions);
    }

    /**
     * Convert seconds to ticks (20 ticks = 1 second)
     */
    private long secondsToTicks(double seconds) {
        return Math.round(seconds * 20.0);
    }

    // ===== Inner Classes =====

    /**
     * Internal task representation
     */
    private static class ScheduledTask {
        private final String id;
        private final Runnable task;
        private long nextExecutionTick;
        private final long intervalTicks;
        private final boolean repeating;
        private volatile boolean cancelled = false;

        public ScheduledTask(String id, Runnable task, long nextExecutionTick, long intervalTicks, boolean repeating) {
            this.id = id;
            this.task = task;
            this.nextExecutionTick = nextExecutionTick;
            this.intervalTicks = intervalTicks;
            this.repeating = repeating;
        }

        public void execute() {
            if (!cancelled) {
                task.run();
            }
        }

        public void scheduleNext(long currentTick) {
            if (repeating && !cancelled) {
                nextExecutionTick = currentTick + intervalTicks;
            }
        }

        public void cancel() {
            cancelled = true;
        }

        // Getters
        public String getId() { return id; }
        public long getNextExecutionTick() { return nextExecutionTick; }
        public long getIntervalTicks() { return intervalTicks; }
        public boolean isRepeating() { return repeating; }
        public boolean isCancelled() { return cancelled; }
    }

    /**
     * Thread-safe counter for limited tasks
     */
    private static class AtomicCounter {
        private volatile int value;

        public AtomicCounter(int initialValue) {
            this.value = initialValue;
        }

        public synchronized int incrementAndGet() {
            return ++value;
        }

        public int get() {
            return value;
        }
    }
}