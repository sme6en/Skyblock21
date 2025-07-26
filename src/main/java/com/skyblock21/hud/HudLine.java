package com.skyblock21.hud;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.text.Text;

public class HudLine {
    // Getters
    @Getter
    private final String id;
    // Setters
    @Getter
    @Setter
    private Text content;
    @Getter
    @Setter
    private String groupId;
    @Getter
    @Setter
    private int order = 0;
    @Getter
    @Setter
    private boolean enabled = true;
    @Getter
    @Setter
    private boolean isDummy = false;

    // Interaction properties
    @Getter
    private boolean clickable = false;
    @Getter
    private boolean hoverable = false;
    private Runnable clickAction;
    @Getter
    private Text hoverText;
    private Text[] multilineHoverText;

    // Visibility control
    @Getter
    @Setter
    private ConditionalVisibilityProvider visibilityProvider;

    public HudLine(String id, Text content) {
        this.id = id;
        this.content = content;
    }

    public HudLine(String id, Text content, String groupId) {
        this.id = id;
        this.content = content;
        this.groupId = groupId;
    }

    public HudLine(String id, Text content, String groupId, int order) {
        this.id = id;
        this.content = content;
        this.groupId = groupId;
        this.order = order;
    }

    // Helper methods
    public boolean hasGroup() { return groupId != null && !groupId.isEmpty(); }

    // Interaction methods
    public void setClickAction(Runnable action) {
        this.clickAction = action;
        this.clickable = action != null;
    }

    public void setHoverText(Text hoverText) {
        this.hoverText = hoverText;
        this.hoverable = hoverText != null;
    }

    public void setMultilineHoverText(String... lines) {
        if (lines.length == 0) {
            this.hoverText = null;
            this.hoverable = false;
            return;
        }

        StringBuilder combined = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            combined.append(lines[i]);
            if (i < lines.length - 1) {
                combined.append("\n");
            }
        }
        setHoverText(Text.literal(combined.toString()));
    }

    public void onClick() {
        if (clickable && clickAction != null) {
            clickAction.run();
        }
    }

    public boolean shouldShow() {
        return enabled && (visibilityProvider == null || visibilityProvider.shouldShow());
    }

    public boolean isSpacer() {
        return false;
    }

    // Sorting support for amount lines
    public int getSortValue() {
        return order;
    }

    public int getDisplayWidth() {
        return content != null ? content.getString().length() : 0;
    }

    @FunctionalInterface
    public interface ConditionalVisibilityProvider {
        boolean shouldShow();
    }
}