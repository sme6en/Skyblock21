package com.skyblock21.hud;

import net.minecraft.text.Text;

public class HudLine {
    private final String id;
    private Text content;
    private boolean enabled;
    private String groupId;
    private int order;
    private boolean clickable;
    private boolean hoverable;
    private Runnable clickAction;
    private Text hoverText;
    private boolean conditionalVisibility;
    private ConditionalVisibilityProvider visibilityProvider;

    public HudLine(String id, Text content) {
        this.id = id;
        this.content = content;
        this.enabled = true;
        this.order = 0;
        this.clickable = false;
        this.hoverable = false;
        this.conditionalVisibility = false;
    }

    public HudLine(String id, Text content, String groupId) {
        this.id = id;
        this.content = content;
        this.enabled = true;
        this.groupId = groupId;
        this.order = 0;
        this.clickable = false;
        this.hoverable = false;
        this.conditionalVisibility = false;
    }

    public HudLine(String id, Text content, String groupId, int order) {
        this.id = id;
        this.content = content;
        this.enabled = true;
        this.groupId = groupId;
        this.order = order;
        this.clickable = false;
        this.hoverable = false;
        this.conditionalVisibility = false;
    }

    // Getters and setters
    public String getId() { return id; }
    public Text getContent() { return content; }
    public void setContent(Text content) { this.content = content; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public boolean hasGroup() { return groupId != null && !groupId.isEmpty(); }

    public int getSortValue() { return order; }
    public int getDisplayWidth() { return 0; }

    // Enhanced features
    public boolean isClickable() { return clickable; }
    public void setClickable(boolean clickable) { this.clickable = clickable; }

    public boolean isHoverable() { return hoverable; }
    public void setHoverable(boolean hoverable) { this.hoverable = hoverable; }

    public Runnable getClickAction() { return clickAction; }
    public void setClickAction(Runnable clickAction) {
        this.clickAction = clickAction;
        this.clickable = clickAction != null;
    }

    public Text getHoverText() { return hoverText; }
    public void setHoverText(Text hoverText) {
        this.hoverText = hoverText;
        this.hoverable = hoverText != null;
    }

    // Convenience method for multiline hover text
    public void setMultilineHoverText(String... lines) {
        if (lines == null || lines.length == 0) {
            setHoverText((Text) null);
            return;
        }

        String combinedText = String.join("\n", lines);
        setHoverText(Text.literal(combinedText));
    }

    // Convenience method for multiline hover with formatting
    public void setHoverTextFormatted(String... lines) {
        if (lines == null || lines.length == 0) {
            setHoverText((Text) null);
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) sb.append("\n");
            sb.append(lines[i]);
        }
        setHoverText(Text.literal(sb.toString()));
    }

    public boolean hasConditionalVisibility() { return conditionalVisibility; }
    public void setConditionalVisibility(boolean conditionalVisibility) {
        this.conditionalVisibility = conditionalVisibility;
    }

    public ConditionalVisibilityProvider getVisibilityProvider() { return visibilityProvider; }
    public void setVisibilityProvider(ConditionalVisibilityProvider visibilityProvider) {
        this.visibilityProvider = visibilityProvider;
        this.conditionalVisibility = visibilityProvider != null;
    }

    public void onClick() {
        if (clickable && clickAction != null) {
            clickAction.run();
        }
    }

    public boolean shouldShow() {
        if (!enabled) return false;
        if (conditionalVisibility && visibilityProvider != null) {
            return visibilityProvider.shouldShow();
        }
        return true;
    }

    @FunctionalInterface
    public interface ConditionalVisibilityProvider {
        boolean shouldShow();
    }
}