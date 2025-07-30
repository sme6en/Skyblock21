package com.skyblock21.hud;

import com.skyblock21.gui.Theme;
import com.skyblock21.gui.ThemeManager;
import com.skyblock21.gui.components.*;
import com.skyblock21.util.ColorUtil;
import com.skyblock21.util.Render2DUtil;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import static net.minecraft.text.Text.literal;

public class EditGuiScreen extends BaseOwoScreen<FlowLayout> {
    private static final int GEAR_ICON_SIZE = 24;
    private static final Identifier GEAR_ICON = Identifier.of("skyblock21", "gui/gear.png");
    public static HudElement selectedElement = null;
    protected final Screen parent;

    public EditGuiScreen(Screen parent) {
        this.parent = parent;

        for (HudElement element : HudManager.getElements()) {
            if (element instanceof MultiLineHudElement multiLineHudElement) {
                multiLineHudElement.recalculateDimensions();
            }
        }
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        Theme theme = ThemeManager.getCurrentTheme();

        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);

        // Instructions container
        FlowLayout instructionsContainer = Containers.verticalFlow(Sizing.content(), Sizing.content());
        instructionsContainer
                .gap(4)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)
                .margins(Insets.of(0, height / 2 + 50, 0, 0));

        // Title
        instructionsContainer.child(new Label(literal("§bSkyBlock§f21§r HUD Editor"))
                .shadow(true)
                .color(Color.ofArgb(ColorUtil.getIntFromColor(theme.getPrimary())))
                .horizontalTextAlignment(HorizontalAlignment.CENTER));

        // Instructions
        instructionsContainer
                .child(new Label(literal("Left Click to Select Element"))
                        .color(Color.WHITE)
                        .horizontalTextAlignment(HorizontalAlignment.CENTER))
                .child(new Label(literal("Right Click to Reset Position"))
                        .color(Color.WHITE)
                        .horizontalTextAlignment(HorizontalAlignment.CENTER))
                .child(new Label(literal("Scroll or \"-\"/\"+\" to increase size"))
                        .color(Color.WHITE)
                        .horizontalTextAlignment(HorizontalAlignment.CENTER))
                .child(new Label(literal("Move selected element with arrows"))
                        .color(Color.WHITE)
                        .horizontalTextAlignment(HorizontalAlignment.CENTER));


        rootComponent.child(instructionsContainer);
        rootComponent.surface(Surface.blur(3, 10));

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render background first
        super.render(context, mouseX, mouseY, delta);

        // Render HUD elements
        renderHudElements(context, mouseX, mouseY);
    }

    private void renderHudElements(DrawContext context, int mouseX, int mouseY) {
        MatrixStack matrices = context.getMatrices();
        float combinedScale = HudManager.getCombinedScale();
        TextRenderer textRenderer = ThemeManager.getCurrentTheme().getTextRenderer();

        for (HudElement element : HudManager.getElements()) {
            if (!element.isEnabled()) continue;

            matrices.push();

            float effectiveX = element.getEffectiveX();
            float effectiveY = element.getEffectiveY();
            float effectiveScale = element.getEffectiveScale();

            matrices.translate(effectiveX, effectiveY, 0);
            matrices.scale(effectiveScale, effectiveScale, 1.0f);

            element.render(context, mouseX, mouseY);

            matrices.pop();

            // Render selection indicators
            if (selectedElement == element) {
                renderSelectionIndicators(context, element, mouseX, mouseY, effectiveX, effectiveY, effectiveScale);
            }

            // Render element info
            if (selectedElement == element && element.isEnabled()) {
                String text = element.getName() + " (" + (int) (element.getScale() * 100) + "%)";
                context.drawTextWithShadow(textRenderer, text,
                        (int) element.getEffectiveX(), (int) element.getEffectiveY() - textRenderer.fontHeight,
                        ColorUtil.getIntFromColor(new java.awt.Color(255, 255, 255, 150)));
            }
        }
    }

    private void renderSelectionIndicators(DrawContext context, HudElement element, int mouseX, int mouseY,
                                           float effectiveX, float effectiveY, float effectiveScale) {
        int scaledWidth = (int) (element.getWidth() * effectiveScale);
        int scaledHeight = (int) (element.getHeight() * effectiveScale);
        int gearIconSize = Math.min(GEAR_ICON_SIZE, (int) (scaledHeight * 0.4f));

        int gearX = (int) (effectiveX + scaledWidth - gearIconSize - 4);
        int gearY = (int) (effectiveY + 4);

        boolean isOverGearIcon = mouseX >= gearX && mouseX <= gearX + gearIconSize &&
                mouseY >= gearY && mouseY <= gearY + gearIconSize;

        // Render gear icon
        context.drawTexture(RenderLayer::getGuiTextured, GEAR_ICON,
                gearX, gearY, 0, 0, gearIconSize, gearIconSize, gearIconSize, gearIconSize,
                ColorUtil.getIntFromColor(new java.awt.Color(255, 255, 255, isOverGearIcon ? 100 : 255)));

        // Render selection outline
        Render2DUtil.drawRoundedBoxOutline(context, effectiveX, effectiveY, scaledWidth, scaledHeight, 2,
                new java.awt.Color(255, 255, 255, 150));
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        for (HudElement element : HudManager.getElements()) {
            if (element.isMouseOver(mouseX, mouseY) && element.isEnabled()) {
                if (button == 1) {
                    element.resetPosition();
                    return true;
                }

                if (button == 0) {
                    float effectiveX = element.getEffectiveX();
                    float effectiveY = element.getEffectiveY();
                    float effectiveScale = element.getEffectiveScale();
                    int scaledWidth = (int) (element.getWidth() * effectiveScale);
                    int scaledHeight = (int) (element.getHeight() * effectiveScale);
                    int gearIconSize = Math.min(GEAR_ICON_SIZE, (int) (scaledHeight * 0.4f));

                    int gearX = (int) (effectiveX + scaledWidth - gearIconSize - 4);
                    int gearY = (int) (effectiveY + 4);

                    boolean isOverGearIcon = mouseX >= gearX && mouseX <= gearX + gearIconSize &&
                            mouseY >= gearY && mouseY <= gearY + gearIconSize;

                    if (isOverGearIcon && selectedElement == element) {
                        client.setScreen(new EditHudElementScreen(this, element));
                        return true;
                    }

                    selectedElement = element;
                    element.startDragging(mouseX, mouseY);
                    return true;
                }
            }
        }

        selectedElement = null;
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (selectedElement != null) {
            selectedElement.stopDragging();
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // Check if scrolling over UI elements first
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }

        // Handle HUD element scaling
        for (HudElement element : HudManager.getElements()) {
            if (element.isMouseOver(mouseX, mouseY) && element.isEnabled()) {
                element.adjustScale((float) verticalAmount * 0.1f);
                return true;
            }
        }
        return false;
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
    public void close() {
        HudManager.saveConfig();
        if (client != null) {
            client.setScreen(parent);
        }
    }
}