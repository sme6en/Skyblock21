package com.skyblock21.features.keyshortcuts;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.Set;

public class Shortcut {
    @SerialEntry
    public String command;
    @SerialEntry
    public int keyCode;
    @SerialEntry
    public Set<Modifier> modifiers;
    @SerialEntry
    public boolean enabled;

    public Shortcut() {
        this.command = "";
        this.keyCode = -1; // Default to an invalid key code
        this.modifiers = Set.of(); // Default to no modifiers
        this.enabled = true; // Default to enabled
    }

    public Shortcut(String command, int keyCode, Set<Modifier> modifiers) {
        this.command = command;
        this.keyCode = keyCode;
        this.modifiers = modifiers;
    }

    public boolean hasModifier(Modifier modifier) {
        return modifiers.contains(modifier);
    }

    private String getMouseButtonName(int mouseButton) {
        return switch (mouseButton) {
            case GLFW.GLFW_MOUSE_BUTTON_LEFT -> "Left Click";
            case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> "Right Click";
            case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> "Middle Click";
            case GLFW.GLFW_MOUSE_BUTTON_4 -> "Mouse 4";
            case GLFW.GLFW_MOUSE_BUTTON_5 -> "Mouse 5";
            case GLFW.GLFW_MOUSE_BUTTON_6 -> "Mouse 6";
            case GLFW.GLFW_MOUSE_BUTTON_7 -> "Mouse 7";
            case GLFW.GLFW_MOUSE_BUTTON_8 -> "Mouse 8";
            default -> "Mouse " + (mouseButton + 1);
        };
    }

    public String getDisplayString() {
        StringBuilder display = new StringBuilder();

        if (hasModifier(Modifier.CTRL)) display.append("Ctrl + ");
        if (hasModifier(Modifier.ALT)) display.append("Alt + ");
        if (hasModifier(Modifier.SHIFT)) display.append("Shift + ");

        if (keyCode != -1) {
            display.append(keyCode >= 1000 ? getMouseButtonName(keyCode - 1000) : getKeyName(keyCode));
        } else {
            display.append("None");
        }

        return display.toString();
    }

    private String getKeyName(int keyCode) {
        return InputUtil.fromKeyCode(keyCode, 0).getLocalizedText().getString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shortcut that = (Shortcut) o;
        return keyCode == that.keyCode &&
                Objects.equals(command, that.command) &&
                Objects.equals(modifiers, that.modifiers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(command, keyCode, modifiers);
    }
}
