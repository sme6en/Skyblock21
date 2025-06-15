package com.skyblock21.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class EditGuiScreen extends Screen {

    public static HudElement selectedElement = null;
    protected final Screen parent;

    public EditGuiScreen(Screen parent) {
        super(Text.literal("Edit SkyBlock21 HUD"));
        this.parent = parent;
    }

    @Override
    public final void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        for (HudElement element : HudManager.getElements()) {
            element.render(context, mouseX, mouseY);
            if ((element.isMouseOver(mouseX, mouseY) || selectedElement == element) && element.isEnabled()) {
                String text = element.getName() + " (" + (int) (element.getScale() * 100) + "%)";
                context.drawTextWithShadow(textRenderer, text, element.getX(), element.getY() - textRenderer.fontHeight, new Color(255, 255, 255, 150).getRGB());
            }
        }

        context.drawCenteredTextWithShadow(textRenderer, "§bSkyBlock§f21§r HUD Editor", width / 2, (height / 2) - textRenderer.fontHeight - 4, Color.GREEN.getRGB());
        context.drawCenteredTextWithShadow(textRenderer, "Right Click to Reset Position", width / 2, height / 2, Color.GRAY.getRGB());
        context.drawCenteredTextWithShadow(textRenderer, "Click \"B\" to Toggle Background", width / 2, (height / 2) + textRenderer.fontHeight + 4, Color.GRAY.getRGB());
        context.drawCenteredTextWithShadow(textRenderer, "Scroll or \"-\"/\"+\" to increase size", width / 2, (height / 2) + (textRenderer.fontHeight + 4) * 2, Color.GRAY.getRGB());
        context.drawCenteredTextWithShadow(textRenderer, "Ctrl + Scroll to increase background opacity", width / 2, (height / 2) + (textRenderer.fontHeight + 4) * 3, Color.GRAY.getRGB());
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (selectedElement != null) {
            selectedElement.dragTo(mouseX, mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public final boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (HudElement element : HudManager.getElements()) {
            if (element.isMouseOver(mouseX, mouseY)) {
                if (button == 1) {
                    element.resetPosition();
                    return true;
                }
                selectedElement = element;
                element.startDragging(mouseX, mouseY);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (selectedElement != null) {
            selectedElement.stopDragging();
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (HudElement element : HudManager.getElements()) {
            if (element.isMouseOver(mouseX, mouseY)) {
                if (hasControlDown()) {
                    element.setBackgroundOpacity(element.getBackgroundOpacity() + (int) verticalAmount);
                } else element.adjustScale((float) verticalAmount * 0.1f);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (selectedElement == null) return super.keyPressed(keyCode, scanCode, modifiers);

        boolean shift = hasShiftDown();
        int increment = shift ? 10 : 1;

        switch (keyCode) {
            case GLFW.GLFW_KEY_UP -> {
                selectedElement.setY(selectedElement.getY() - increment);
                return true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                selectedElement.setY(selectedElement.getY() + increment);
                return true;
            }
            case GLFW.GLFW_KEY_LEFT -> {
                selectedElement.setX(selectedElement.getX() - increment);
                return true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                selectedElement.setX(selectedElement.getX() + increment);
                return true;
            }
            case GLFW.GLFW_KEY_EQUAL, GLFW.GLFW_KEY_KP_EQUAL -> {
                selectedElement.adjustScale(shift ? 0.1f : 0.05f);
                return true;
            }
            case GLFW.GLFW_KEY_MINUS, GLFW.GLFW_KEY_KP_SUBTRACT -> {
                selectedElement.adjustScale(shift ? -0.1f : -0.05f);
                return true;
            }
            case GLFW.GLFW_KEY_B -> {
                selectedElement.setBackgroundEnabled(!selectedElement.isBackgroundEnabled());
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public final void close() {
        HudManager.saveConfig();
        client.setScreen(parent);
    }
}
