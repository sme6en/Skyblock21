package com.skyblock21.tracking;

import lombok.Getter;

public class TrackableValue<T extends Number> {
    @Getter
    private T total;
    private double ratePerHour = 0;
    @Getter
    private long lastReceivedTime = -1;
    @Getter
    private long timesReceived = 0;

    // Price tracking
    @Getter
    private double pricePerItem = 0.0;
    @Getter
    private boolean hasPricing = false;

    @SuppressWarnings("unchecked")
    public TrackableValue(T initialValue) {
        // Create zero value of the same type
        if (initialValue instanceof Integer) {
            this.total = (T) Integer.valueOf(0);
        } else if (initialValue instanceof Long) {
            this.total = (T) Long.valueOf(0L);
        } else if (initialValue instanceof Double) {
            this.total = (T) Double.valueOf(0.0);
        } else if (initialValue instanceof Float) {
            this.total = (T) Float.valueOf(0.0f);
        } else {
            this.total = initialValue;
        }
    }

    @SuppressWarnings("unchecked")
    public void add(T increment) {
        if (total instanceof Integer && increment instanceof Integer) {
            total = (T) Integer.valueOf(total.intValue() + increment.intValue());
        } else if (total instanceof Long && increment instanceof Long) {
            total = (T) Long.valueOf(total.longValue() + increment.longValue());
        } else if (total instanceof Double && increment instanceof Double) {
            total = (T) Double.valueOf(total.doubleValue() + increment.doubleValue());
        } else if (total instanceof Float && increment instanceof Float) {
            total = (T) Float.valueOf(total.floatValue() + increment.floatValue());
        } else {
            // Handle mixed types by converting to double
            double currentValue = total.doubleValue();
            double incrementValue = increment.doubleValue();
            double newValue = currentValue + incrementValue;

            // Convert back to the original type
            if (total instanceof Integer) {
                total = (T) Integer.valueOf((int) newValue);
            } else if (total instanceof Long) {
                total = (T) Long.valueOf((long) newValue);
            } else if (total instanceof Float) {
                total = (T) Float.valueOf((float) newValue);
            } else {
                total = (T) Double.valueOf(newValue);
            }
        }

        lastReceivedTime = System.currentTimeMillis();
        timesReceived++;
    }

    public void updateRate(double hoursActive) {
        if (hoursActive > 0) {
            ratePerHour = total.doubleValue() / hoursActive;
        }
    }

    public long getTimeSinceLastReceived() {
        if (lastReceivedTime == -1) return -1;
        return System.currentTimeMillis() - lastReceivedTime;
    }

    public double getRatePerHour() {
        return ratePerHour;
    }

    // Price-related methods
    public void setPricePerItem(double price) {
        this.pricePerItem = price;
        this.hasPricing = price > 0;
    }

    public double getTotalValue() {
        return total.doubleValue() * pricePerItem;
    }

    public double getValuePerHour() {
        return ratePerHour * pricePerItem;
    }

    // Safe conversion methods to avoid casting issues
    public int asInt() {
        return total.intValue();
    }

    public long asLong() {
        return total.longValue();
    }

    public double asDouble() {
        return total.doubleValue();
    }

    public float asFloat() {
        return total.floatValue();
    }

    public boolean isType(Class<? extends Number> type) {
        return type.isInstance(total);
    }

    // Safe casting with fallback
    @SuppressWarnings("unchecked")
    public <U extends Number> U getAs(Class<U> type, U defaultValue) {
        try {
            if (type == Integer.class) {
                return (U) Integer.valueOf(total.intValue());
            } else if (type == Long.class) {
                return (U) Long.valueOf(total.longValue());
            } else if (type == Double.class) {
                return (U) Double.valueOf(total.doubleValue());
            } else if (type == Float.class) {
                return (U) Float.valueOf(total.floatValue());
            }
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}