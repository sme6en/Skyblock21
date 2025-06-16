package com.skyblock21.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import static net.minecraft.text.Text.literal;

public class EditHudElementScreen extends Screen {

    public static HudElement element = null;
    private final Screen parent;

    private SliderWidget scaleSlider;
    private CheckboxWidget backgroundCheckbox;
    private SliderWidget backgroundOpacitySlider;
    private ButtonWidget resetButton;
    private ButtonWidget doneButton;
    private ButtonWidget cancelButton;

    private static final int SETTINGS_PANEL_WIDTH = 220;
    private static final int SETTINGS_PANEL_HEIGHT = 240;
    private static final int SETTINGS_PANEL_MARGIN = 20;

    private boolean isDragging = false;
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;

    protected EditHudElementScreen(Screen parent, HudElement _element) {
        super(literal("Editing " + _element.getName() + " Element"));
        element = _element;
        this.parent = parent;

        HudManager.saveConfig();
    }

    @Override
    protected void init() {
        super.init();

        boolean elementOnRight = element.getX() > width / 2;
        int settingsX = elementOnRight ? SETTINGS_PANEL_MARGIN : width - SETTINGS_PANEL_WIDTH - SETTINGS_PANEL_MARGIN;
        int settingsY = (height - SETTINGS_PANEL_HEIGHT) / 2;
        int widgetWidth = 180;
        int widgetHeight = 20;
        int spacing = 30;
        int widgetStartY = settingsY + 60;

        this.backgroundCheckbox = CheckboxWidget.builder(literal("Background"), textRenderer)
                                                .callback((checkbox, checked) -> {
                                                    element.setBackgroundEnabled(checked);
                                                    backgroundOpacitySlider.active = checked;
                                                })
                                                .pos(settingsX + 20, widgetStartY)
                                                .maxWidth(widgetWidth)
                                                .checked(element.isBackgroundEnabled())
                                                .build();
        addSelectableChild(backgroundCheckbox);

        this.backgroundOpacitySlider = new SliderWidget(settingsX + 20, widgetStartY + spacing, widgetWidth, widgetHeight,
                literal("Background Opacity: " + element.getBackgroundOpacity() + "%"), element.getBackgroundOpacity() / 100.0) {
            @Override
            protected void updateMessage() {
                setMessage(literal("Background Opacity: " + (int) (value * 100) + "%"));
            }

            @Override
            protected void applyValue() {
                int opacity = (int) (value * 100);
                element.setBackgroundOpacity(opacity);
            }
        };
        backgroundOpacitySlider.active = element.isBackgroundEnabled();
        addSelectableChild(backgroundOpacitySlider);

        double scaleValue = (element.getScale() - 0.5) / 3.5;
        this.scaleSlider = new SliderWidget(settingsX + 20, widgetStartY + spacing * 2, widgetWidth, widgetHeight,
                literal("Scale: " + (int) (element.getScale() * 100) + "%"), scaleValue) {
            @Override
            protected void updateMessage() {
                double scale = 0.5 + (value * 3.5);
                setMessage(literal("Scale: " + (int) (scale * 100) + "%"));
            }

            @Override
            protected void applyValue() {
                double scale = 0.5 + (value * 3.5);
                element.setScale((float) scale);
            }
        };
        addSelectableChild(scaleSlider);

        this.resetButton = ButtonWidget.builder(
                                               Text.literal("Reset to Default"),
                                               button -> resetToDefaults())
                                       .dimensions(settingsX + 20, widgetStartY + spacing * 3 + 20, widgetWidth, widgetHeight)
                                       .build();
        addSelectableChild(resetButton);

        this.doneButton = ButtonWidget.builder(
                                              Text.literal("Done"),
                                              button -> {
                                                  HudManager.saveConfig(); // Save the changes
                                                  close();
                                              })
                                      .dimensions(settingsX + 20, settingsY + SETTINGS_PANEL_HEIGHT - 35, 85, widgetHeight)
                                      .build();
        addSelectableChild(doneButton);

        this.cancelButton = ButtonWidget.builder(
                                                Text.literal("Cancel"),
                                                button -> {
                                                    HudManager.loadConfig(); // Revert changes
                                                    close();
                                                })
                                        .dimensions(settingsX + 115, settingsY + SETTINGS_PANEL_HEIGHT - 35, 85, widgetHeight)
                                        .build();
        addSelectableChild(cancelButton);
    }

    private void repositionWidgets() {
        boolean elementOnRight = element.getX() > width / 2;
        int newSettingsX = elementOnRight ? SETTINGS_PANEL_MARGIN : width - SETTINGS_PANEL_WIDTH - SETTINGS_PANEL_MARGIN;
        int settingsY = (height - SETTINGS_PANEL_HEIGHT) / 2;
        int widgetStartY = settingsY + 60;
        int spacing = 30;

        if (backgroundCheckbox != null) {
            backgroundCheckbox.setX(newSettingsX + 20);
            backgroundCheckbox.setY(widgetStartY);
        }
        if (backgroundOpacitySlider != null) {
            backgroundOpacitySlider.setX(newSettingsX + 20);
            backgroundOpacitySlider.setY(widgetStartY + spacing);
        }
        if (scaleSlider != null) {
            scaleSlider.setX(newSettingsX + 20);
            scaleSlider.setY(widgetStartY + spacing * 2);
        }
        if (resetButton != null) {
            resetButton.setX(newSettingsX + 20);
            resetButton.setY(widgetStartY + spacing * 3 + 20);
        }
        if (doneButton != null) {
            doneButton.setX(newSettingsX + 20);
            doneButton.setY(settingsY + SETTINGS_PANEL_HEIGHT - 35);
        }
        if (cancelButton != null) {
            cancelButton.setX(newSettingsX + 115);
            cancelButton.setY(settingsY + SETTINGS_PANEL_HEIGHT - 35);
        }
    }


    private void resetToDefaults() {
        element.resetDefaults();

        backgroundOpacitySlider.active = element.isBackgroundEnabled();
    }

    @Override
    public final void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        MatrixStack matrices = context.getMatrices();

        context.drawCenteredTextWithShadow(textRenderer, literal("§bSkyblock§f21"), width / 2, 20, 0xFFFFFF);

        matrices.push();
        matrices.translate((float) width / 2, 35, 0);
        matrices.scale(1.3f, 1.3f, 1.0f);
        context.drawCenteredTextWithShadow(textRenderer, this.title, 0, 0, 0xFFFFFF);
        matrices.pop();

        matrices.push();
        matrices.translate(element.getX(), element.getY(), 0);
        matrices.scale(element.getScale(), element.getScale(), 1.0f);
        element.render(context, mouseX, mouseY);
        matrices.pop();

        int elementWidth = (int)(element.getWidth() * element.getScale());
        int elementHeight = (int)(element.getHeight() * element.getScale());
        context.drawBorder(element.getX() - 1, element.getY() - 1, elementWidth + 2, elementHeight + 2, 0xFFFFFFFF);

        boolean elementOnRight = element.getX() > width / 2;
        int settingsX = elementOnRight ? SETTINGS_PANEL_MARGIN : width - SETTINGS_PANEL_WIDTH - SETTINGS_PANEL_MARGIN;
        int settingsY = (height - SETTINGS_PANEL_HEIGHT) / 2;

        context.fill(settingsX, settingsY, settingsX + SETTINGS_PANEL_WIDTH, settingsY + SETTINGS_PANEL_HEIGHT, 0x80000000);
        context.drawBorder(settingsX, settingsY, SETTINGS_PANEL_WIDTH, SETTINGS_PANEL_HEIGHT, 0xFFFFFFFF);

        String settingsTitle = element.getName() + " Settings";
        int titleWidth = textRenderer.getWidth(settingsTitle);
        int titleX = settingsX + (SETTINGS_PANEL_WIDTH - titleWidth) / 2;
        context.drawTextWithShadow(textRenderer, settingsTitle, titleX, settingsY + 15, 0xFFFFFF);

        context.drawTextWithShadow(textRenderer, "Appearance:", settingsX + 20, settingsY + 45, 0xCCCCCC);

        boolean isNearBottom = element.getY() + element.getHeight() * element.getScale() > (float) height * 0.8f;
        String coords = String.format("(%d, %d)", element.getX(), element.getY());
        context.drawTextWithShadow(textRenderer, coords, element.getX() + 2, isNearBottom ? element.getY() - textRenderer.fontHeight : (int) (element.getY() + element.getHeight() * element.getScale() + 5)
                , 0xCCCCCC);

        if (!isDragging) {
            context.drawCenteredTextWithShadow(textRenderer, "Click and drag the element to move it", width / 2, height - 30, 0xCCCCCC);
        } else {
            context.drawCenteredTextWithShadow(textRenderer, "Dragging... Release to place", width / 2, height - 30, 0xFFFF55);
        }

        backgroundCheckbox.render(context, mouseX, mouseY, delta);
        backgroundOpacitySlider.render(context, mouseX, mouseY, delta);
        scaleSlider.render(context, mouseX, mouseY, delta);
        resetButton.render(context, mouseX, mouseY, delta);
        doneButton.render(context, mouseX, mouseY, delta);
        cancelButton.render(context, mouseX, mouseY, delta);

        if (backgroundCheckbox.isHovered()) {
            context.drawTooltip(textRenderer, Text.literal("Toggle background behind the element"), mouseX, mouseY);
        } else if (backgroundOpacitySlider.isHovered()) {
            context.drawTooltip(textRenderer, Text.literal("Adjust background transparency (0% - 100%)"), mouseX, mouseY);
        } else if (scaleSlider.isHovered()) {
            context.drawTooltip(textRenderer, Text.literal("Size scaling of the element (50% - 400%)"), mouseX, mouseY);
        } else if (resetButton.isHovered()) {
            context.drawTooltip(textRenderer, Text.literal("Reset all settings to default values"), mouseX, mouseY);
        }

    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (button == 1) {
            element.resetPosition();
            repositionWidgets();
            return true;
        }

        if (button == 0) {
            int elementWidth = (int)(element.getWidth() * element.getScale());
            int elementHeight = (int)(element.getHeight() * element.getScale());

            if (mouseX >= element.getX() && mouseX <= element.getX() + elementWidth &&
                    mouseY >= element.getY() && mouseY <= element.getY() + elementHeight) {
                isDragging = true;
                dragOffsetX = mouseX - element.getX();
                dragOffsetY = mouseY - element.getY();
                return true;
            }
        }

        return false;
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
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (super.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }

        if (button == 0 && isDragging) {
            isDragging = false;
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
            int newX = (int)(mouseX - dragOffsetX);
            int newY = (int)(mouseY - dragOffsetY);

            newX = Math.max(0, Math.min(newX, width - (int)(element.getWidth() * element.getScale())));
            newY = Math.max(0, Math.min(newY, height - (int)(element.getHeight() * element.getScale())));

            element.setX(newX);
            element.setY(newY);

            repositionWidgets();

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
                return true;
            }
            case GLFW.GLFW_KEY_MINUS, GLFW.GLFW_KEY_KP_SUBTRACT -> {
                element.adjustScale(shift ? -0.1f : -0.05f);
                return true;
            }
            case GLFW.GLFW_KEY_ESCAPE -> {
                close();
                return true;
            }
        }

        return true;
    }
}
