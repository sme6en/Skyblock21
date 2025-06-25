package com.skyblock21.hud;

import net.minecraft.client.Mouse;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.function.Function;

public class EditGuiScreen extends Screen {

    private static final int GEAR_ICON_SIZE = 24;
    private static final Identifier GEAR_ICON = Identifier.of("skyblock21", "gui/gear.png");
    public static HudElement selectedElement = null;
    protected final Screen parent;

    public EditGuiScreen(Screen parent) {
        super(Text.literal("Edit SkyBlock21 HUD"));
        this.parent = parent;
    }

    @Override
    public final void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        MatrixStack matrices = context.getMatrices();
        for (HudElement element : HudManager.getElements()) {
            matrices.push();
            matrices.translate(element.getX(), element.getY(), 0);
            matrices.scale(element.getScale(), element.getScale(), 1.0f);

            element.render(context, mouseX, mouseY);

            matrices.pop();


            if (selectedElement == element) {
                int gearIconSize = Math.min(GEAR_ICON_SIZE, (int) (element.getHeight() * element.getScale() * 0.4f));

                boolean isOverGearIcon = mouseX >= (element.getX() + (element.getWidth() * element.getScale()) - gearIconSize - 4) && mouseX <= (element.getX() + element.getWidth() * element.getScale()) - 4 && mouseY >= element.getY() + 4 && mouseY <= (element.getY() + gearIconSize + 4);

                context.drawTexture(RenderLayer::getGuiTextured, GEAR_ICON, (int) (element.getX() + (element.getWidth() * element.getScale()) - gearIconSize - 4), element.getY() + 4, 0, 0, gearIconSize, gearIconSize, gearIconSize, gearIconSize, new Color(255, 255, 255, isOverGearIcon ? 100 : 255).getRGB());
                context.drawBorder(element.getX(), element.getY(), (int) (element.getWidth() * element.getScale()), (int) (element.getHeight() * element.getScale()), new Color(255, 255, 255, 150).getRGB());
            }


            if (selectedElement == element && element.isEnabled()) {
                String text = element.getName() + " (" + (int) (element.getScale() * 100) + "%)";
                context.drawTextWithShadow(textRenderer, text, 0, -textRenderer.fontHeight, new Color(255, 255, 255, 150).getRGB());
            }

        }

        matrices.push();
        matrices.translate(width / 2f, (height / 2f) - textRenderer.fontHeight - 12, 0);
        matrices.scale(1.4f, 1.4f, 1.0f);
        context.drawCenteredTextWithShadow(textRenderer, "§bSkyBlock§f21§r HUD Editor", 0, 0, Color.GREEN.getRGB());
        matrices.pop();
        context.drawCenteredTextWithShadow(textRenderer, "Left Click to Select Element", width / 2, height / 2, Color.GRAY.getRGB());
        context.drawCenteredTextWithShadow(textRenderer, "Right Click to Reset Position", width / 2, (height / 2) + (textRenderer.fontHeight + 4), Color.GRAY.getRGB());
        context.drawCenteredTextWithShadow(textRenderer, "Scroll or \"-\"/\"+\" to increase size", width / 2, (height / 2) + (textRenderer.fontHeight + 4) * 2, Color.GRAY.getRGB());
        context.drawCenteredTextWithShadow(textRenderer, "Move selected element with arrows", width / 2, (height / 2) + (textRenderer.fontHeight + 4) * 3, Color.GRAY.getRGB());
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
            if (element.isMouseOver(mouseX, mouseY) && element.isEnabled()) {
                if (button == 1) {
                    element.resetPosition();
                    return true;
                }

                int gearIconSize = Math.min(GEAR_ICON_SIZE, (int) (element.getHeight() * element.getScale() * 0.4f));
                boolean isOverGearIcon = mouseX >= (element.getX() + (element.getWidth() * element.getScale()) - gearIconSize - 4) && mouseX <= (element.getX() + element.getWidth() * element.getScale()) - 4 && mouseY >= element.getY() + 4 && mouseY <= (element.getY() + gearIconSize + 4);


                if (isOverGearIcon && selectedElement == element) {
                    client.setScreen(new EditHudElementScreen(this, element));
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
            if (element.isMouseOver(mouseX, mouseY) && element.isEnabled()) {
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
