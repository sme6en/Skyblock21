package com.skyblock21.hud;

import java.util.*;
import com.google.gson.*;

public class HudGroup {
    private final String id;
    private String displayName;
    private boolean enabled;
    private int order;
    private SortType sortType;
    private boolean ascending;
    private boolean alignAmounts;

    public HudGroup(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
        this.enabled = true;
        this.order = 0;
        this.sortType = SortType.ORDER;
        this.ascending = true;
        this.alignAmounts = false;
    }

    public HudGroup(String id, String displayName, int order) {
        this(id, displayName);
        this.order = order;
    }

    public HudGroup(String id, String displayName, int order, boolean alignAmounts) {
        this(id, displayName, order);
        this.alignAmounts = alignAmounts;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public SortType getSortType() {
        return sortType;
    }

    public void setSortType(SortType sortType) {
        this.sortType = sortType;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public boolean isAlignAmounts() {
        return alignAmounts;
    }

    public void setAlignAmounts(boolean alignAmounts) {
        this.alignAmounts = alignAmounts;
    }

    public Comparator<HudLine> getComparator() {
        Comparator<HudLine> comparator = switch (sortType) {
            case ORDER -> Comparator.comparingInt(HudLine::getOrder);
            case NAME_LENGTH -> Comparator.comparingInt(line -> {
                if (line instanceof HudAmountLine amountLine) {
                    return amountLine.getItemName().length();
                }
                return line.getContent().getString().length();
            });
            case AMOUNT -> Comparator.comparingLong(line -> {
                if (line instanceof HudAmountLine amountLine) {
                    return amountLine.getAmount();
                }
                return 0;
            });
            case CUSTOM -> Comparator.comparingInt(line -> {
                if (line instanceof HudAmountLine amountLine) {
                    return amountLine.getCustomValue();
                }
                return line.getSortValue();
            });
        };

        return ascending ? comparator : comparator.reversed();
    }

    public int getMaxItemNameWidth(List<HudLine> lines) {
        if (!alignAmounts) return 0;

        return lines.stream().filter(line -> line instanceof HudAmountLine).mapToInt(HudLine::getDisplayWidth).max().orElse(0);
    }

    public JsonObject saveToJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("enabled", enabled);
        obj.addProperty("order", order);
        obj.addProperty("sortType", sortType.name());
        obj.addProperty("ascending", ascending);
        obj.addProperty("alignAmounts", alignAmounts);
        return obj;
    }

    public void loadFromJson(JsonObject obj) {
        if (obj.has("enabled")) enabled = obj.get("enabled").getAsBoolean();
        if (obj.has("order")) order = obj.get("order").getAsInt();
        if (obj.has("sortType")) {
            try {
                sortType = SortType.valueOf(obj.get("sortType").getAsString());
            } catch (IllegalArgumentException e) {
                sortType = SortType.ORDER;
            }
        }
        if (obj.has("ascending")) ascending = obj.get("ascending").getAsBoolean();
        if (obj.has("alignAmounts")) alignAmounts = obj.get("alignAmounts").getAsBoolean();
    }
}
