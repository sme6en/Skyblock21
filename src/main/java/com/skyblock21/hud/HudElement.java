package com.skyblock21.hud;

import com.skyblock21.util.Location;
import com.skyblock21.util.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.util.EnumSet;
import java.util.Set;

public abstract class HudElement {

    protected int HORIZONTAL_PADDING = 2;
    protected int VERTICAL_PADDING = 2;
    private final String name;
    public boolean alwaysRenderDummy = false;
    protected Set<Location> locationsShown = EnumSet.allOf(Location.class);
    private int x;
    private int y;
    private int defaultX = 0;
    private int defaultY = 0;
    private float scale = 1.0f;
    private boolean dragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private boolean enabled = true;
    private boolean backgroundEnabled = false;
    private int backgroundOpacity = 40;

    public HudElement(int x, int y) {
        this.name = getClass().getSimpleName().replace("Element", "");
        this.x = x;
        this.y = y;
        this.defaultX = x;
        this.defaultY = y;
    }

    public HudElement(int x, int y, Location location) {
        this(x, y);
        this.locationsShown = EnumSet.of(location);
    }

    public HudElement(int x, int y, Location location, boolean alwaysDummy) {
        this(x, y);
        this.locationsShown = EnumSet.of(location);
        this.alwaysRenderDummy = alwaysDummy;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) {
            this.x = x;
            return;
        }

        float combinedScale = HudManager.getCombinedScale();
        int maxX = (int) ((client.getWindow().getWidth() / combinedScale) - (getWidth() * scale) - 1);

        this.x = Math.max(1, Math.min(x, maxX));
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) {
            this.y = y;
            return;
        }

        float combinedScale = HudManager.getCombinedScale();
        int maxY = (int) ((client.getWindow().getHeight() / combinedScale) - (getHeight() * scale) - 1);

        this.y = Math.max(1, Math.min(y, maxY));
    }

    public float getEffectiveX() {
        return x * HudManager.getCombinedScale();
    }

    public float getEffectiveY() {
        return y * HudManager.getCombinedScale();
    }

    public float getEffectiveScale() {
        return scale * HudManager.getCombinedScale();
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;

        setX(this.x);
        setY(this.y);
    }

    public boolean isBackgroundEnabled() {
        return backgroundEnabled;
    }

    public void setBackgroundEnabled(boolean enabled) {
        this.backgroundEnabled = enabled;
    }

    public int getBackgroundOpacity() {
        return backgroundOpacity;
    }

    public void setBackgroundOpacity(int opacity) {
        this.backgroundOpacity = Math.max(0, Math.min(opacity, 100));
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.currentScreen instanceof EditGuiScreen || client.currentScreen instanceof EditHudElementScreen) {
            renderBackground(context);
            if (shouldRenderDummy()) {
                renderDummy(context);
            } else {
                renderElement(context);
            }
        } else if (isAllowedInLocation(Utils.getLocation()) && isEnabled()) {
            if (!shouldRenderDummy()) renderBackground(context);
            renderElement(context);
        }
    }

    public void renderBackground(DrawContext context) {
        if (isBackgroundEnabled() && isEnabled()) {
            context.fill(0, 0, getWidth(), getHeight(), new Color(0, 0, 0, ((int) (255 * backgroundOpacity) / 100)).getRGB());
        }
    }

    protected abstract void renderElement(DrawContext context);

    protected abstract void renderDummy(DrawContext context);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void toggle() {
        this.enabled = !this.enabled;
    }

    public boolean isAllowedInLocation(Location location) {
        return locationsShown.contains(location);
    }

    public void showInAllLocations() {
        this.locationsShown = EnumSet.allOf(Location.class);
    }

    public void showInLocation(Location location) {
        this.locationsShown = EnumSet.of(location);
    }

    public boolean shouldRenderDummy() {
        return alwaysRenderDummy || !isAllowedInLocation(Utils.getLocation()) || !isEnabled();
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        float effectiveX = getEffectiveX();
        float effectiveY = getEffectiveY();
        float effectiveScale = getEffectiveScale();
        int width = getWidth();
        int height = getHeight();

        return mouseX >= effectiveX && mouseX <= effectiveX + width * effectiveScale &&
                mouseY >= effectiveY && mouseY <= effectiveY + height * effectiveScale;
    }

    public void startDragging(double mouseX, double mouseY) {
        dragging = true;
        float combinedScale = HudManager.getCombinedScale();
        dragOffsetX = (int) ((mouseX / combinedScale) - x);
        dragOffsetY = (int) ((mouseY / combinedScale) - y);
    }

    public void stopDragging() {
        dragging = false;
    }

    public void dragTo(double mouseX, double mouseY) {
        if (dragging) {
            float combinedScale = HudManager.getCombinedScale();
            setX((int) ((mouseX / combinedScale) - dragOffsetX));
            setY((int) ((mouseY / combinedScale) - dragOffsetY));
        }
    }

    public void adjustScale(float delta) {
        scale += delta;
        scale = Math.max(0.5f, Math.min(4.0f, scale));

        setX(x);
        setY(y);
    }

    public void resetPosition() {
        setX(defaultX);
        setY(defaultY);
    }

    public void resetDefaults() {
        setBackgroundEnabled(false);
        setScale(1f);
        resetPosition();
    }

    public abstract int getWidth();

    public abstract int getHeight();

}
