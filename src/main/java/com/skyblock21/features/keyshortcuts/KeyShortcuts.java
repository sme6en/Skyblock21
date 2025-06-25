package com.skyblock21.features.keyshortcuts;

import com.skyblock21.config.persistent.PersistentData;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class KeyShortcuts {

    private static final MinecraftClient client = MinecraftClient.getInstance();

    private static final Set<Integer> pressedKeys = new HashSet<>();

    public static void init() {

        ClientTickEvents.END_CLIENT_TICK.register(KeyShortcuts::onTick);
    }

    private static void onTick(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        Set<Integer> currentPressed = new HashSet<>();
        long window = client.getWindow().getHandle();

        boolean ctrlPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
        boolean altPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS;
        boolean shiftPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;

        for (Shortcut shortcut : PersistentData.get().shortcuts) {
            if (!shortcut.enabled || shortcut.keyCode == 0) continue;

            boolean isPressed = false;

            if (shortcut.keyCode >= 1000) {
                int mouseButton = shortcut.keyCode - 1000;
                isPressed = GLFW.glfwGetMouseButton(window, mouseButton) == GLFW.GLFW_PRESS;
            } else {
                // Keyboard key
                isPressed = GLFW.glfwGetKey(window, shortcut.keyCode) == GLFW.GLFW_PRESS;
            }

            boolean wasPressed = pressedKeys.contains(shortcut.keyCode);

            if (isPressed && !wasPressed) {
                // Check if modifiers match exactly
                boolean ctrlMatch = shortcut.hasModifier(Modifier.CTRL) == ctrlPressed;
                boolean altMatch = shortcut.hasModifier(Modifier.ALT) == altPressed;
                boolean shiftMatch = shortcut.hasModifier(Modifier.SHIFT) == shiftPressed;

                if (ctrlMatch && altMatch && shiftMatch) {
                    executeShortcut(shortcut);
                }
            }

            if (isPressed) {
                currentPressed.add(shortcut.keyCode);
            }
        }

        pressedKeys.clear();
        pressedKeys.addAll(currentPressed);
    }

    private static void executeShortcut(Shortcut shortcut) {
        if (client.player == null) return;
        if (client.currentScreen != null) return;

        String command = shortcut.command;
        if (!command.startsWith("/")) {
            command = "/" + command;
        }
        client.player.networkHandler.sendChatCommand(command.substring(1));
    }
}
