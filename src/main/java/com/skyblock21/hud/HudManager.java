package com.skyblock21.hud;

import com.google.gson.JsonObject;
import com.skyblock21.Skyblock21;
import com.skyblock21.events.SkyblockEvents;
import com.skyblock21.util.Location;
import com.skyblock21.util.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class HudManager {

    public static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("skyblock21_hud.json");
    public static final List<HudElement> hudElements = new ArrayList<>();

    // Reference resolution for scaling calculations (1920x1080 as baseline)
    private static final int REFERENCE_WIDTH = 1920;
    private static final int REFERENCE_HEIGHT = 1080;

    // Store the resolution when positions were last saved
    private static int savedWidth = REFERENCE_WIDTH;
    private static int savedHeight = REFERENCE_HEIGHT;

    // Mouse interaction state
    private static HudLine currentHoveredLine = null;
    private static MultiLineHudElement currentHoveredElement = null;

    public static void init() {
        loadConfig();
        HudLayerRegistrationCallback.EVENT.register((layeredDrawerWrapper ->
                layeredDrawerWrapper.attachLayerAfter(IdentifiedLayer.OVERLAY_MESSAGE,
                        Identifier.of("skyblock21", "hud_overlay"), HudManager::render)));
        ClientTickEvents.END_CLIENT_TICK.register(HudManager::onTick);
        SkyblockEvents.JOIN.register(HudManager::onJoin);

        // Add container interaction support
        ScreenEvents.AFTER_INIT.register(HudManager::onScreenInit);
    }

    private static void onScreenInit(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) {
        if (screen instanceof HandledScreen<?>) {
            // Register render events for container screens
            ScreenEvents.afterRender(screen).register(HudManager::onContainerRender);
        }
    }

    private static void onContainerRender(Screen screen, DrawContext context, int mouseX, int mouseY, float delta) {
        // Update hover state
        handleMouseMove(mouseX, mouseY);

        // Render HUD elements over container
        renderHudElements(context);

        // Render hover tooltips
        renderHoverTooltip(context, mouseX, mouseY);
    }

    private static void onJoin() {
        for (HudElement element : hudElements) {
            if (element instanceof MultiLineHudElement multiLineHudElement) {
                multiLineHudElement.recalculateDimensions();
            }
        }
    }

    /**
     * Handles mouse clicks on HUD elements
     */
    public static boolean handleMouseClick(double mouseX, double mouseY, int button) {
        if (button != 0) return false; // Only handle left clicks

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof EditGuiScreen ||
                client.currentScreen instanceof EditHudElementScreen) {
            return false; // Don't handle clicks in edit mode
        }

        Location location = Utils.getLocation();

        for (HudElement element : hudElements) {
            if (!element.isEnabled() || !element.isAllowedInLocation(location)) continue;

            if (element instanceof MultiLineHudElement multiLineElement) {
                if (element.isMouseOver(mouseX, mouseY)) {
                    if (multiLineElement.handleClick(mouseX, mouseY)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Handles mouse movement for hover effects
     */
    public static void handleMouseMove(double mouseX, double mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof EditGuiScreen ||
                client.currentScreen instanceof EditHudElementScreen) {
            return; // Don't handle hover in edit mode
        }

        Location location = Utils.getLocation();
        HudLine newHoveredLine = null;
        MultiLineHudElement newHoveredElement = null;

        for (HudElement element : hudElements) {
            if (!element.isEnabled() || !element.isAllowedInLocation(location)) continue;

            if (element instanceof MultiLineHudElement multiLineElement) {
                if (element.isMouseOver(mouseX, mouseY)) {
                    HudLine hoveredLine = multiLineElement.getHoveredLine(mouseX, mouseY);
                    if (hoveredLine != null) {
                        newHoveredLine = hoveredLine;
                        newHoveredElement = multiLineElement;
                        break;
                    }
                }
            }
        }

        currentHoveredLine = newHoveredLine;
        currentHoveredElement = newHoveredElement;
    }

    /**
     * Renders hover tooltip if applicable
     */
    public static void renderHoverTooltip(DrawContext drawContext, int mouseX, int mouseY) {
        if (currentHoveredLine != null && currentHoveredLine.isHoverable() &&
                currentHoveredLine.getHoverText() != null) {

            Text hoverText = currentHoveredLine.getHoverText();
            drawContext.drawTooltip(MinecraftClient.getInstance().textRenderer, hoverText, mouseX, mouseY);
        }
    }

    /**
     * Renders HUD elements (shared between normal and container rendering)
     */
    private static void renderHudElements(DrawContext drawContext) {
        Location location = Utils.getLocation();
        MatrixStack matrices = drawContext.getMatrices();
        float combinedScale = getCombinedScale();

        MinecraftClient client = MinecraftClient.getInstance();
        boolean inEditMode = client.currentScreen instanceof EditGuiScreen ||
                client.currentScreen instanceof EditHudElementScreen;

        for (HudElement element : hudElements) {
            // In edit mode, show all elements (including disabled ones for positioning)
            if (inEditMode) {
                matrices.push();
                float scaledX = element.getX() * combinedScale;
                float scaledY = element.getY() * combinedScale;
                matrices.translate(scaledX, scaledY, 0);
                float finalScale = element.getScale() * combinedScale;
                matrices.scale(finalScale, finalScale, 1.0f);
                element.render(drawContext, 0, 0);
                matrices.pop();
            } else {
                // Normal gameplay - only show enabled elements in correct location
                if (!element.isEnabled() || !element.isAllowedInLocation(location)) continue;

                matrices.push();
                float scaledX = element.getX() * combinedScale;
                float scaledY = element.getY() * combinedScale;
                matrices.translate(scaledX, scaledY, 0);
                float finalScale = element.getScale() * combinedScale;
                matrices.scale(finalScale, finalScale, 1.0f);
                element.render(drawContext, 0, 0);
                matrices.pop();
            }
        }
    }

    /**
     * Calculates the scale factor for responsive scaling based on screen size
     */
    public static float getViewportScale() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getWindow() == null) return 1.0f;

        int currentWidth = client.getWindow().getWidth();
        int currentHeight = client.getWindow().getHeight();

        // Calculate scale based on both width and height, use the smaller scale to ensure everything fits
        float widthScale = (float) currentWidth / REFERENCE_WIDTH;
        float heightScale = (float) currentHeight / REFERENCE_HEIGHT;

        return Math.min(widthScale, heightScale);
    }

    /**
     * Gets the GUI scale compensation factor
     */
    public static float getGuiScaleCompensation() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getWindow() == null) return 1.0f;
        double guiScale = client.getWindow().getScaleFactor();
        return (float) (1.0 / Math.max(1.0, guiScale / 2.0));
    }

    /**
     * Gets the combined scale factor (viewport + GUI compensation)
     */
    public static float getCombinedScale() {
        return getViewportScale() * getGuiScaleCompensation();
    }

    private static void render(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Don't render if in edit screens
        if (client.currentScreen instanceof EditGuiScreen ||
                client.currentScreen instanceof EditHudElementScreen) return;

        // Skip if container is open (handled by container renderer)
        if (client.currentScreen instanceof HandledScreen<?>) return;

        renderHudElements(drawContext);

        // Render hover tooltip for normal gameplay
        if (client.mouse != null) {
            double mouseX = client.mouse.getX() * client.getWindow().getScaleFactor();
            double mouseY = client.mouse.getY() * client.getWindow().getScaleFactor();
            renderHoverTooltip(drawContext, (int) mouseX, (int) mouseY);
        }
    }

    private static void onTick(MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        if (!Utils.isOnSkyblock()) return;

        for (HudElement element : hudElements) {
            if (!element.isEnabled() || !element.isAllowedInLocation(Utils.getLocation())) continue;

            element.onTick(client);
        }

        if (client.mouse != null && !(client.currentScreen instanceof HandledScreen<?>)) {
            double mouseX = client.mouse.getX() * client.getWindow().getScaleFactor();
            double mouseY = client.mouse.getY() * client.getWindow().getScaleFactor();
            handleMouseMove(mouseX, mouseY);
        }
    }

    public static void register(HudElement element) {
        hudElements.add(element);
    }

    public static List<HudElement> getElements() {
        return hudElements;
    }

    /**
     * Converts screen coordinates to reference coordinates for storage
     */
    public static int toReferenceCoordinate(int screenCoordinate, boolean isWidth) {
        float currentScale = getCombinedScale();
        if (currentScale == 0) return screenCoordinate;

        return Math.round(screenCoordinate / currentScale);
    }

    /**
     * Converts reference coordinates to current screen coordinates
     */
    public static int toScreenCoordinate(int referenceCoordinate, boolean isWidth) {
        float currentScale = getCombinedScale();
        return Math.round(referenceCoordinate * currentScale);
    }

    public static void loadConfig() {
        if (!Files.exists(CONFIG_FILE)) {
            saveConfig();
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(CONFIG_FILE)) {
            JsonObject object = Skyblock21.GSON.fromJson(reader, JsonObject.class);
            if (object == null) return;

            // Load saved resolution if available
            if (object.has("savedWidth")) {
                savedWidth = object.get("savedWidth").getAsInt();
            }
            if (object.has("savedHeight")) {
                savedHeight = object.get("savedHeight").getAsInt();
            }

            JsonObject positions = object.getAsJsonObject("positions");
            if (positions == null) return;

            for (HudElement element : hudElements) {
                if (positions.has(element.getName())) {
                    JsonObject obj = positions.getAsJsonObject(element.getName());

                    if (obj == null) {
                        Skyblock21.LOGGER.warn("No position data found for HUD element: {}", element.getName());
                        continue;
                    }

                    // Load positions (these are stored in reference coordinates)
                    if (obj.has("x")) element.setX(obj.get("x").getAsInt());
                    if (obj.has("y")) element.setY(obj.get("y").getAsInt());
                    if (obj.has("scale")) element.setScale(obj.get("scale").getAsFloat());
                    if (obj.has("enabled")) {
                        element.setEnabled(obj.get("enabled").getAsBoolean());
                    } else {
                        element.setEnabled(true);
                    }
                    if (obj.has("backgroundEnabled")) {
                        element.setBackgroundEnabled(obj.get("backgroundEnabled").getAsBoolean());
                    } else {
                        element.setBackgroundEnabled(false);
                    }

                    if (obj.has("backgroundOpacity")) {
                        element.setBackgroundOpacity(obj.get("backgroundOpacity").getAsInt());
                    } else {
                        element.setBackgroundOpacity(40);
                    }

                    if (element instanceof MultiLineHudElement multiLineElement) {
                        if (obj.has("lineStates")) {
                            JsonObject lineStates = obj.getAsJsonObject("lineStates");
                            multiLineElement.loadLineStates(lineStates);
                        }
                    }
                }
            }

        } catch (Exception e) {
            Skyblock21.LOGGER.error("Failed to load HUD config", e);
            e.printStackTrace();
        }
    }

    public static void saveConfig() {
        JsonObject root = new JsonObject();
        JsonObject positions = new JsonObject();

        // Save current resolution as reference
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getWindow() != null) {
            root.addProperty("savedWidth", client.getWindow().getWidth());
            root.addProperty("savedHeight", client.getWindow().getHeight());
        }

        for (HudElement element : hudElements) {
            JsonObject obj = new JsonObject();

            // Save positions in reference coordinates
            obj.addProperty("x", element.getX());
            obj.addProperty("y", element.getY());
            obj.addProperty("scale", element.getScale());
            obj.addProperty("enabled", element.isEnabled());
            obj.addProperty("backgroundEnabled", element.isBackgroundEnabled());
            obj.addProperty("backgroundOpacity", element.getBackgroundOpacity());

            if (element instanceof MultiLineHudElement multiLineElement) {
                JsonObject lineStates = multiLineElement.saveLineStates();
                obj.add("lineStates", lineStates);
            }

            positions.add(element.getName(), obj);
        }

        root.add("positions", positions);

        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            Files.writeString(CONFIG_FILE, Skyblock21.GSON.toJson(root));
        } catch (Exception e) {
            Skyblock21.LOGGER.error("Failed to save HUD config", e);
            e.printStackTrace();
        }
    }
}