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

    protected final int HORIZONTAL_PADDING = 2;
    protected final int VERTICAL_PADDING = 2;
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
        this.x = MinecraftClient.getInstance() == null || MinecraftClient.getInstance()
                                                                         .getWindow() == null ? x : Math.max(0, Math.min(x, MinecraftClient.getInstance()
                                                                                                                                           .getWindow()
                                                                                                                                           .getScaledWidth() - (int) (getWidth() * scale)));
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = MinecraftClient.getInstance() == null || MinecraftClient.getInstance()
                                                                         .getWindow() == null ? y : Math.max(0, Math.min(y, MinecraftClient.getInstance()
                                                                                                                                           .getWindow()
                                                                                                                                           .getScaledHeight() - (int) (getHeight() * scale)));
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
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
        MatrixStack matrices = context.getMatrices();
        TextRenderer textRenderer = client.textRenderer;

        matrices.push();
        matrices.translate(x, y, 0);
        matrices.scale(scale, scale, 1);


        if (client.currentScreen instanceof EditGuiScreen) {
            if (isEnabled()) {
                renderBackground(context);
            }
            if (shouldRenderDummy()) {
                renderDummy(context);
            } else {
                renderElement(context);
            }
        } else if (isAllowedInLocation(Utils.getLocation()) && isEnabled()) {
            if (isBackgroundEnabled()) renderBackground(context);
            renderElement(context);
        }

        matrices.pop();
    }

    public void renderBackground(DrawContext context) {
        if (isBackgroundEnabled() && isEnabled()) {
            context.fill(0, 0, getWidth(), getHeight(), new Color(0, 0, 0, ((int) (255 * backgroundOpacity) / 100)).getRGB());
        }
        if (MinecraftClient.getInstance().currentScreen instanceof EditGuiScreen) {
            if (EditGuiScreen.selectedElement == this) {
                // highlight the selected element with stroke
                context.drawBorder(0, 0, getWidth(), getHeight(), new Color(255, 255, 255, 150).getRGB());
            }
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

    public boolean shouldRenderDummy() {
        return alwaysRenderDummy || !isAllowedInLocation(Utils.getLocation()) || !isEnabled();
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        int width = getWidth();
        int height = getHeight();
        return mouseX >= x && mouseX <= x + width * scale &&
                mouseY >= y && mouseY <= y + height * scale;
    }

    public void startDragging(double mouseX, double mouseY) {
        dragging = true;
        dragOffsetX = (int) (mouseX - x);
        dragOffsetY = (int) (mouseY - y);
    }

    public void stopDragging() {
        dragging = false;
    }

    public void dragTo(double mouseX, double mouseY) {
        if (dragging) {
            setX((int) (mouseX - dragOffsetX));
            setY((int) (mouseY - dragOffsetY));
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
