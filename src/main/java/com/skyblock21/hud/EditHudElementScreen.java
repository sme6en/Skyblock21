package com.skyblock21.hud;

import com.skyblock21.gui.ThemeManager;
import com.skyblock21.gui.components.*;
import com.skyblock21.util.ColorUtil;
import com.skyblock21.util.Render2DUtil;
import com.skyblock21.util.TickSchedulerHelper;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import static net.minecraft.text.Text.literal;

public class EditHudElementScreen extends BaseOwoScreen<FlowLayout> {

    public static HudElement element = null;
    private final Screen parent;
    Animation animation;
    private Slider scaleSlider;
    private Checkbox backgroundCheckbox;
    private Slider backgroundOpacitySlider;
    private boolean isDragging = false;
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;
    private boolean rightLastTime = false;

    protected EditHudElementScreen(Screen parent, HudElement _element) {
        this.parent = parent;
        element = _element;
        HudManager.saveConfig();
    }

    @Override
    public void init() {
        super.init();
        animation.forwards();
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        var theme = ThemeManager.getCurrentTheme();

        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);


        // Determine settings panel position based on element position
        boolean elementOnRight = element.getEffectiveX() > width / 2;
        rightLastTime = elementOnRight;

        // Settings panel
        var settingsContainer = new RoundedContainer(
                Sizing.fixed(240),
                Sizing.fixed(0),
                theme.getRounding(),
                theme.getBackground(),
                FlowLayout.Algorithm.VERTICAL
        );

        animation = settingsContainer.verticalSizing().animate(500, Easing.CUBIC, Sizing.content());

        settingsContainer
                .surface(Surface.VANILLA_TRANSLUCENT)
                .padding(Insets.of(20));
        settingsContainer.gap(10);

        if (elementOnRight) {
            settingsContainer.positioning(Positioning.absolute(20, height / 2 - 150));
        } else {
            settingsContainer.positioning(Positioning.absolute(width - 260, height / 2 - 150));
        }

        settingsContainer.child(new Label(literal(element.getName() + " Settings"))
                                 .color(Color.ofArgb(ColorUtil.getIntFromColor(theme.primary)))
                                 .horizontalTextAlignment(HorizontalAlignment.LEFT));

        settingsContainer.child(new Label(literal("Appearance:"))
                .color(Color.ofArgb(ColorUtil.getIntFromColor(theme.text))));

        backgroundCheckbox = new Checkbox(literal("Background"), (checkbox, checked) -> {
            element.setBackgroundEnabled(checked);
        })
                .checked(element.isBackgroundEnabled());

        settingsContainer.child(backgroundCheckbox);

        backgroundOpacitySlider = (Slider) new Slider(Sizing.fill(100), 0, 100)
            .value(element.getBackgroundOpacity())
            .message(s -> literal("Background Opacity: " + element.getBackgroundOpacity() + "%"));

        backgroundOpacitySlider.onChanged().subscribe(value -> {
            element.setBackgroundOpacity((int) value);
            backgroundOpacitySlider.setMessage(literal("Background Opacity: " + value + "%"));
        });

        settingsContainer.child(backgroundOpacitySlider);
        double scaleValue = (element.getScale() - 0.5) / 3.5 * 100;
        // Scale slider
        scaleSlider = (Slider) new Slider(Sizing.fill(100), 0, 100).value(scaleValue)
                                                                   .message(s -> literal("Scale: " + (s) + "%"));

        scaleSlider.onChanged().subscribe(value -> {
            double scale = 0.5 + (value / 100.0 * 3.5);
            element.setScale((float) scale);
            scaleSlider.setMessage(literal("Scale: " + (int) (value) + "%"));
        });

        settingsContainer.child(scaleSlider);

        // Reset button
        Button resetButton = (Button) new Button(
                literal("Reset to default"),
                theme.getSecondaryBackground(),
                theme.getPrimary(),
                button -> resetToDefaults()
        ).textShadow(false).horizontalSizing(Sizing.fill(100));

        settingsContainer.child(resetButton);

        // Bottom buttons
        FlowLayout buttonsContainer = (FlowLayout) Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).horizontalAlignment(HorizontalAlignment.CENTER);
        buttonsContainer.gap(10);

        buttonsContainer
                .child(new Button(literal("Done"), theme.getPrimary(), theme.getBackground(), button -> {
                    HudManager.saveConfig();
                    close();
                }).horizontalSizing(Sizing.fill(47)))
                .child(new Button(literal("Cancel"), theme.getSecondaryBackground(), theme.getPrimary(), button -> {
                    HudManager.loadConfig();
                    close();
                }).textShadow(false).horizontalSizing(Sizing.fill(47)));

        settingsContainer.child(buttonsContainer);

        // Instructions
        FlowLayout instructionsContainer = Containers.verticalFlow(Sizing.content(), Sizing.content());
        instructionsContainer
                .gap(4)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .positioning(Positioning.absolute(width / 2 - 150, height - 80));


        rootComponent.child(settingsContainer);
        rootComponent.child(instructionsContainer);

        updateBackgroundOpacityState();
    }

    private void updateBackgroundOpacityState() {
        if (backgroundOpacitySlider != null) {
            // The slider should be enabled/disabled based on background checkbox
            // This is handled in the checkbox change event
        }
    }

    private void resetToDefaults() {
        element.resetDefaults();

        // Update UI elements
        if (backgroundCheckbox != null) {
            backgroundCheckbox.checked(element.isBackgroundEnabled());
        }
        if (backgroundOpacitySlider != null) {
            backgroundOpacitySlider.value(element.getBackgroundOpacity());
            backgroundOpacitySlider.setMessage(literal("Background Opacity: " + element.getBackgroundOpacity() + "%"));
        }
        if (scaleSlider != null) {
            double scaleValue = (element.getScale() - 0.5) / 3.5 * 100;
            scaleSlider.value((int) scaleValue);
            scaleSlider.setMessage(literal("Scale: " + (int) (scaleValue) + "%"));
        }

        updateBackgroundOpacityState();

        // Rebuild to reposition widgets
        boolean elementOnRight = element.getEffectiveX() > width / 2;
        if (elementOnRight != rightLastTime) {
            rightLastTime = elementOnRight;
            animation.backwards();
            TickSchedulerHelper.runAfter(() -> {
                uiAdapter.rootComponent.clearChildren();
                build(uiAdapter.rootComponent);
                animation.forwards();
            }, 5);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        MatrixStack matrices = context.getMatrices();

        // Render the HUD element being edited
        matrices.push();
        float effectiveX = element.getEffectiveX();
        float effectiveY = element.getEffectiveY();
        float effectiveScale = element.getEffectiveScale();

        matrices.translate(effectiveX, effectiveY, 0);
        matrices.scale(effectiveScale, effectiveScale, 1.0f);
        element.render(context, mouseX, mouseY);
        matrices.pop();

        // Render selection border
        int elementWidth = (int) (element.getWidth() * effectiveScale);
        int elementHeight = (int) (element.getHeight() * effectiveScale);
        Render2DUtil.drawRoundedBoxOutline(context, effectiveX - 1, effectiveY - 1, elementWidth + 2, elementHeight + 2, 2, new java.awt.Color(255, 255, 255, 150));

        // Render UI components
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        if (button == 1) { // Right click
            element.resetPosition();
            // Rebuild to reposition widgets
            uiAdapter.rootComponent.clearChildren();
            build(uiAdapter.rootComponent);
            return true;
        }

        if (button == 0) { // Left click on element
            float effectiveX = element.getEffectiveX();
            float effectiveY = element.getEffectiveY();
            int elementWidth = (int) (element.getWidth() * element.getEffectiveScale());
            int elementHeight = (int) (element.getHeight() * element.getEffectiveScale());

            if (mouseX >= effectiveX && mouseX <= effectiveX + elementWidth &&
                    mouseY >= effectiveY && mouseY <= effectiveY + elementHeight) {
                isDragging = true;

                float combinedScale = HudManager.getCombinedScale();
                dragOffsetX = (mouseX / combinedScale) - element.getX();
                dragOffsetY = (mouseY / combinedScale) - element.getY();
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }

        if (element.isMouseOver(mouseX, mouseY)) {
            element.adjustScale((float) verticalAmount * 0.1f);

            // Update scale slider
            if (scaleSlider != null) {
                double scaleValue = (element.getScale() - 0.5) / 3.5 * 100;
                scaleSlider.value((int) scaleValue);
                scaleSlider.setMessage(literal("Scale: " + (int) (element.getScale() * 100) + "%"));
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (super.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }

        if (button == 0 && isDragging) {
            isDragging = false;
            boolean elementOnRight = element.getEffectiveX() > width / 2;
            if (elementOnRight != rightLastTime) {
                rightLastTime = elementOnRight;
                animation.backwards();
                TickSchedulerHelper.runAfter(() -> {
                    uiAdapter.rootComponent.clearChildren();
                    build(uiAdapter.rootComponent);
                    animation.forwards();
                }, 5);
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }

        if (button == 0 && isDragging) {
            float combinedScale = HudManager.getCombinedScale();
            int newX = (int) ((mouseX / combinedScale) - dragOffsetX);
            int newY = (int) ((mouseY / combinedScale) - dragOffsetY);

            int maxX = (int) ((width / combinedScale) - (element.getWidth() * element.getScale()));
            int maxY = (int) ((height / combinedScale) - (element.getHeight() * element.getScale()));

            newX = Math.max(0, Math.min(newX, maxX));
            newY = Math.max(0, Math.min(newY, maxY));

            element.setX(newX);
            element.setY(newY);

            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean shift = hasShiftDown();
        int increment = shift ? 10 : 1;

        switch (keyCode) {
            case GLFW.GLFW_KEY_UP -> {
                element.setY(element.getY() - increment);
                return true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                element.setY(element.getY() + increment);
                return true;
            }
            case GLFW.GLFW_KEY_LEFT -> {
                element.setX(element.getX() - increment);
                return true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                element.setX(element.getX() + increment);
                return true;
            }
            case GLFW.GLFW_KEY_EQUAL, GLFW.GLFW_KEY_KP_EQUAL -> {
                element.adjustScale(shift ? 0.1f : 0.05f);
                // Update scale slider
                if (scaleSlider != null) {
                    double scaleValue = (element.getScale() - 0.5) / 3.5 * 100;
                    scaleSlider.value((int) scaleValue);
                    scaleSlider.setMessage(literal("Scale: " + (int) (element.getScale() * 100) + "%"));
                }
                return true;
            }
            case GLFW.GLFW_KEY_MINUS, GLFW.GLFW_KEY_KP_SUBTRACT -> {
                element.adjustScale(shift ? -0.1f : -0.05f);
                // Update scale slider
                if (scaleSlider != null) {
                    double scaleValue = (element.getScale() - 0.5) / 3.5 * 100;
                    scaleSlider.value((int) scaleValue);
                    scaleSlider.setMessage(literal("Scale: " + (int) (element.getScale() * 100) + "%"));
                }
                return true;
            }
            case GLFW.GLFW_KEY_ESCAPE -> {
                close();
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }
}