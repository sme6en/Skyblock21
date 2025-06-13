package com.skyblock21.hud;

import com.skyblock21.util.Utils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class EditGuiScreen extends Screen {

    protected final Screen parent;
    public static HudElement selectedElement = null;

    public EditGuiScreen(Screen parent) {
        super(Text.literal("Edit SkyBlock21 HUD"));
        this.parent = parent;
    }

    @Override
    public final void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        for (HudElement element : HudManager.getElements()) {
            if ((!element.isEnabled() && !element.isAllowedInLocation(Utils.getLocation())) || element.alwaysRenderDummy) {
                MatrixStack matrices = context.getMatrices();
                matrices.push();
                matrices.translate(element.getX(), element.getY(), 0);
                matrices.scale(element.getScale(), element.getScale(), 1);
                element.renderBackground(context);
                element.renderDummy(context);
                matrices.pop();
            } else {
                element.render(context);
            }
        }

        context.drawCenteredTextWithShadow(textRenderer, "SkyBlock21 HUD Editor", width / 2, (height / 2) - textRenderer.fontHeight - 5, Color.GREEN.getRGB());
        context.drawCenteredTextWithShadow(textRenderer, "Right Click To Reset Position", width / 2, height / 2, Color.GRAY.getRGB());
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
                element.adjustScale((float) verticalAmount * 0.1f);
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
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public final void close() {
        HudManager.saveConfig();
        client.setScreen(parent);
    }
}
