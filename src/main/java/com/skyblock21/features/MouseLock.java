package com.skyblock21.features;

import com.skyblock21.util.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class MouseLock {

    private static final KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("Lock Mouse", GLFW.GLFW_KEY_O, "SkyBlock 21"));

    public static boolean isMouseLocked = false;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!Utils.isOnSkyblock()) return;
            while (keyBinding.wasPressed()) {
                isMouseLocked = !isMouseLocked;
            }
        });

        HudLayerRegistrationCallback.EVENT.register(d -> d.attachLayerAfter(IdentifiedLayer.OVERLAY_MESSAGE, Identifier.of("skyblock21", "mouse_lock_status"), ((context, tickCounter) -> render(context))));
    }

    public static void render(DrawContext context) {
        if (MinecraftClient.getInstance().currentScreen instanceof HandledScreen<?> || MinecraftClient.getInstance().currentScreen instanceof Screen)
            return;
        if (!isMouseLocked) return;

        int screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, "Mouse locked", (screenWidth / 2), (screenHeight / 2) + 20, new Color(255, 89, 48).getRGB());
    }
}
