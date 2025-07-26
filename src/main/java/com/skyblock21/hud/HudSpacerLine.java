package com.skyblock21.hud;

import net.minecraft.text.Text;

public class HudSpacerLine extends HudLine {
    private final int height;

    public HudSpacerLine(String id) {
        this(id, 1);
    }

    public HudSpacerLine(String id, int height) {
        super(id, Text.empty());
        this.height = Math.max(1, height);
        this.setEnabled(true);
    }

    public HudSpacerLine(String id, int height, String groupId) {
        super(id, Text.empty(), groupId);
        this.height = Math.max(1, height);
        this.setEnabled(true);
    }

    public HudSpacerLine(String id, int height, boolean isDummy) {
        super(id, Text.empty());
        this.height = Math.max(1, height);
        this.setEnabled(true);
        this.setDummy(isDummy);
    }

    public HudSpacerLine(String id, int height, String groupId, boolean isDummy) {
        super(id, Text.empty(), groupId);
        this.height = Math.max(1, height);
        this.setEnabled(true);
        this.setDummy(isDummy);
    }

    public int getSpacerHeight() {
        return height;
    }

    @Override
    public boolean isSpacer() {
        return true;
    }

    @Override
    public boolean shouldShow() {
        return isEnabled() && (getVisibilityProvider() == null || getVisibilityProvider().shouldShow());
    }
}