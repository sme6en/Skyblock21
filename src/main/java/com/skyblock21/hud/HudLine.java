package com.skyblock21.hud;

import net.minecraft.text.Text;

public class HudLine {
    private final String id;
    private Text content;
    private boolean enabled;
    private String groupId;
    private int order;

    public HudLine(String id, Text content) {
        this.id = id;
        this.content = content;
        this.enabled = true;
        this.order = 0;
    }

    public HudLine(String id, Text content, String groupId) {
        this.id = id;
        this.content = content;
        this.enabled = true;
        this.groupId = groupId;
        this.order = 0;
    }

    public HudLine(String id, Text content, String groupId, int order) {
        this.id = id;
        this.content = content;
        this.enabled = true;
        this.groupId = groupId;
        this.order = order;
    }

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
}