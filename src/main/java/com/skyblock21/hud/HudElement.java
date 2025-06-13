package com.skyblock21.hud;

import com.skyblock21.util.Location;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.util.EnumSet;
import java.util.Set;

public abstract class HudElement {

    private final String name;

    private int x;
    private int y;
    private int defaultX = 0;
    private int defaultY = 0;
    private float scale = 1.0f;

    private boolean dragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    private boolean enabled = true;
    public boolean alwaysRenderDummy = false;

    protected Set<Location> locationsShown = EnumSet.allOf(Location.class);

    public HudElement(int x, int y) {
        this.name = getClass().getSimpleName();
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
        this.x = MinecraftClient.getInstance() == null || MinecraftClient.getInstance().getWindow() == null ? x :
    Math.max(0, Math.min(x, MinecraftClient.getInstance().getWindow().getScaledWidth() - (int)(getWidth() * scale)));
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = MinecraftClient.getInstance() == null || MinecraftClient.getInstance().getWindow() == null ? y : Math.max(0, Math.min(y, MinecraftClient.getInstance().getWindow().getScaledHeight() - (int)(getHeight() * scale)));
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void render(DrawContext context) {
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x, y, 0);
        matrices.scale(scale, scale, 1);
        renderBackground(context);
        renderElement(context);
        matrices.pop();
    }

    public void renderBackground(DrawContext context) {
        if (MinecraftClient.getInstance().currentScreen != null && MinecraftClient.getInstance().currentScreen instanceof EditGuiScreen) {
            context.fill(0, 0, getWidth(), getHeight(), new Color(255,255,255, 30).getRGB()); // semi-transparent background

            if (EditGuiScreen.selectedElement == this) {
                context.fill(0, 0, getWidth(), getHeight(), new Color(255,255,255, 60).getRGB()); // light-gray background for selected element
            }
        }
    }

    protected abstract void renderElement(DrawContext context);
    protected abstract void renderDummy(DrawContext context);

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        this.enabled = !this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAllowedInLocation(Location location) {
        return locationsShown.contains(location);
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        int width = getWidth();
        int height = getHeight();
        return mouseX >= x && mouseX <= x + width * scale &&
                mouseY >= y && mouseY <= y + height * scale;
    }

    public void startDragging(double mouseX, double mouseY) {
        dragging = true;
        dragOffsetX = (int)(mouseX - x);
        dragOffsetY = (int)(mouseY - y);
    }

    public void stopDragging() {
        dragging = false;
    }

    public void dragTo(double mouseX, double mouseY) {
        if (dragging) {
            setX((int)(mouseX - dragOffsetX));
            setY((int)(mouseY - dragOffsetY));
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

    public abstract int getWidth();
    public abstract int getHeight();

}
