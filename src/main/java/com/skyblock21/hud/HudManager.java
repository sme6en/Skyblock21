package com.skyblock21.hud;

import com.google.gson.JsonObject;
import com.skyblock21.Skyblock21;
import com.skyblock21.util.Location;
import com.skyblock21.util.Utils;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class HudManager {


    public static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("skyblock21_hud.json");
    public static final List<HudElement> hudElements = new ArrayList<>();

    public static void init() {
        loadConfig();
        HudLayerRegistrationCallback.EVENT.register((layeredDrawerWrapper -> layeredDrawerWrapper.attachLayerAfter(IdentifiedLayer.OVERLAY_MESSAGE, Identifier.of("skyblock21", "hud_overlay"), HudManager::render)));
    }

    private static void render(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        if (MinecraftClient.getInstance().currentScreen instanceof EditGuiScreen) return;
        Location location = Utils.getLocation();
        MatrixStack matrices = drawContext.getMatrices();
        for (HudElement element : hudElements) {
            if (!element.isEnabled() || !element.isAllowedInLocation(location)) continue;

            matrices.push();
            matrices.translate(element.getX(), element.getY(), 0);
            matrices.scale(element.getScale(), element.getScale(), 1.0f);

            if (EditHudElementScreen.element != null && EditHudElementScreen.element != element) element.render(drawContext,0,0);

            matrices.pop();
        }
    }

    public static void register(HudElement element) {
        hudElements.add(element);
    }

    public static List<HudElement> getElements() {
        return hudElements;
    }

    public static void loadConfig() {
        if (!Files.exists(CONFIG_FILE)) {
            saveConfig(); // Create default config file if you want
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(CONFIG_FILE)) {
            JsonObject object = Skyblock21.GSON.fromJson(reader, JsonObject.class);
            JsonObject positions = object.getAsJsonObject("positions");

            for (HudElement element : hudElements) {
                if (positions.has(element.getName())) {
                    JsonObject obj = positions.getAsJsonObject(element.getName());

                    if (obj == null) {
                        Skyblock21.LOGGER.warn("No position data found for HUD element: {}", element.getName());
                        continue; // Skip if no position data
                    }

                    if (obj.has("x")) element.setX(obj.get("x").getAsInt());
                    if (obj.has("y")) element.setY(obj.get("y").getAsInt());
                    if (obj.has("scale")) element.setScale(obj.get("scale").getAsFloat());
                    if (obj.has("enabled")) {
                        element.setEnabled(obj.get("enabled").getAsBoolean());
                    } else {
                        element.setEnabled(true); // Default to enabled if not specified
                    }
                    if (obj.has("backgroundEnabled")) {
                        element.setBackgroundEnabled(obj.get("backgroundEnabled").getAsBoolean());
                    } else {
                        element.setBackgroundEnabled(false); // Default to false if not specified
                    }

                    if (obj.has("backgroundOpacity")) {
                        element.setBackgroundOpacity(obj.get("backgroundOpacity").getAsInt());
                    } else {
                        element.setBackgroundOpacity(40);
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

        for (HudElement element : hudElements) {
            JsonObject obj = new JsonObject();
            obj.addProperty("x", element.getX());
            obj.addProperty("y", element.getY());
            obj.addProperty("scale", element.getScale());
            obj.addProperty("enabled", element.isEnabled());
            obj.addProperty("backgroundEnabled", element.isBackgroundEnabled());
            obj.addProperty("backgroundOpacity", element.getBackgroundOpacity());
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
