package com.skyblock21.features.itemcustomization;

import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.util.TextUtils;
import com.skyblock21.util.Utils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class ItemCustomizationScreen extends Screen {
    private final ItemStack itemStack;
    private final String itemUuid;
    private ItemCustomization customization;

    private TextFieldWidget nameField;
    private TextFieldWidget itemIdField;
    private CheckboxWidget glintCheckbox;
    private ButtonWidget saveButton;
    private ButtonWidget resetButton;

    private String errorMessage = "";
    private int errorTicks = 0;

    public ItemCustomizationScreen(ItemStack itemStack) {
        super(Text.literal("Item Customization"));
        this.itemStack = itemStack;
        this.itemUuid = Utils.getItemUUID(itemStack);

        // Load existing customization or create new one
        this.customization = PersistentData.get().itemCustomizations.getOrDefault(
                itemUuid,
                new ItemCustomization()
        );
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int startY = this.height / 2 - 80;

        // Item name field
        this.nameField = new TextFieldWidget(this.textRenderer, centerX - 100, startY, 200, 20, Text.literal(""));
        this.nameField.setMaxLength(100);
        this.nameField.setText(customization.customName);
        this.nameField.setChangedListener(this::onNameChanged);
        this.addSelectableChild(this.nameField);

        // Item ID field
        this.itemIdField = new TextFieldWidget(this.textRenderer, centerX - 100, startY + 40, 200, 20, Text.literal(""));
        this.itemIdField.setMaxLength(100);
        this.itemIdField.setText(customization.customItemId);
        this.itemIdField.setChangedListener(this::onItemIdChanged);
        this.addSelectableChild(this.itemIdField);

        // Glint checkbox
        this.glintCheckbox = CheckboxWidget.builder(Text.literal("Enable Glint"), this.textRenderer)
                                           .pos(centerX - 50, startY + 80)
                                           .checked(customization.hasGlint)
                                           .build();
        this.addDrawableChild(this.glintCheckbox);

        // Save button
        this.saveButton = ButtonWidget.builder(Text.literal("Save"), button -> this.save())
                                      .dimensions(centerX - 105, startY + 120, 100, 20)
                                      .build();
        this.addDrawableChild(this.saveButton);

        // Reset button
        this.resetButton = ButtonWidget.builder(Text.literal("Reset"), button -> this.reset())
                                       .dimensions(centerX + 5, startY + 120, 100, 20)
                                       .build();
        this.addDrawableChild(this.resetButton);

        // Close button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Close"), button -> this.close())
                                          .dimensions(centerX - 50, startY + 150, 100, 20)
                                          .build());
    }

    private void onNameChanged(String newValue) {
        // Just trigger a re-render for preview update
        // No validation needed for names
    }

    private void onItemIdChanged(String newValue) {
        if (!newValue.isEmpty()) {
            try {
                Identifier id = Identifier.tryParse(newValue);
                if (id == null) {
                    setError("Invalid item ID format");
                    return;
                }

                Item item = Registries.ITEM.get(id);
                if (item == null || item.equals(Registries.ITEM.get(Identifier.of("air")))) {
                    setError("Item not found: " + newValue);
                    return;
                }

                clearError();
            } catch (Exception e) {
                setError("Invalid item ID: " + e.getMessage());
            }
        } else {
            clearError();
        }
    }

    private void setError(String message) {
        this.errorMessage = message;
        this.errorTicks = 100;
        this.saveButton.active = false;
    }

    private void clearError() {
        this.errorMessage = "";
        this.errorTicks = 0;
        this.saveButton.active = true;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.errorTicks > 0) {
            this.errorTicks--;
            if (this.errorTicks == 0) {
                clearError();
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int startY = this.height / 2 - 80;

        // Title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, startY - 80, 0xFFFFFF);

        ItemStack previewStack = createPreviewStack();

        // Draw large item preview (48x48 instead of 16x16)
        context.getMatrices().push();
        context.getMatrices().scale(3.0f, 3.0f, 1.0f);
        context.drawItem(previewStack, (centerX - 24) / 3, (startY - 60) / 3);
        context.getMatrices().pop();

        // Labels
        context.drawTextWithShadow(this.textRenderer, "Custom Name:", centerX - 100, startY - 10, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "Custom Item ID:", centerX - 100, startY + 30, 0xFFFFFF);

        // Draw fields
        this.nameField.render(context, mouseX, mouseY, delta);
        this.itemIdField.render(context, mouseX, mouseY, delta);

        // Draw error message
        if (!errorMessage.isEmpty() && errorTicks > 0) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal(errorMessage).formatted(Formatting.RED),
                    centerX,
                    startY + 100,
                    0xFF5555
            );
        }

        // Draw color code help
        context.drawTextWithShadow(
                this.textRenderer,
                Text.literal("Use & for color codes (e.g., &a for green)").formatted(Formatting.GRAY),
                centerX - 100,
                startY + 60,
                0x888888
        );
    }

    private ItemStack createPreviewStack() {
        ItemStack preview = this.itemStack.copy();

        // Apply custom item type if valid
        String customItemId = this.itemIdField.getText().trim();
        if (!customItemId.isEmpty()) {
            try {
                Identifier id = Identifier.tryParse(customItemId);
                if (id != null) {
                    Item customItem = Registries.ITEM.get(id);
                    if (customItem != null && !customItem.equals(Registries.ITEM.get(Identifier.of("air")))) {
                        preview = new ItemStack(customItem, preview.getCount());
                        // Copy over the original NBT data but with new item
                        preview.set(DataComponentTypes.CUSTOM_DATA, this.itemStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT));
                    }
                }
            } catch (Exception ignored) {}
        }

        // Apply custom name if present
        String customName = this.nameField.getText().trim();
        if (!customName.isEmpty()) {
            String formattedName = TextUtils.translateColorCodes(customName);
            preview.set(DataComponentTypes.CUSTOM_NAME, Text.literal(formattedName));
        }

        // Apply glint
        if (this.glintCheckbox.isChecked()) {
            preview.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        } else {
            preview.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false);
        }

        return preview;
    }

    private void save() {
        // Validate item ID before saving
        String itemId = this.itemIdField.getText().trim();
        if (!itemId.isEmpty()) {
            ItemCustomization temp = new ItemCustomization("", itemId, false);
            if (!temp.isValidItemId()) {
                setError("Cannot save: Invalid item ID");
                return;
            }
        }

        ItemCustomization newCustomization = new ItemCustomization(
                this.nameField.getText(),
                itemId,
                this.glintCheckbox.isChecked()
        );

        if (newCustomization.hasCustomName() || newCustomization.hasCustomItemId() || newCustomization.hasGlint) {
            PersistentData.get().itemCustomizations.put(itemUuid, newCustomization);
        } else {
            PersistentData.get().itemCustomizations.remove(itemUuid);
        }

        PersistentData.save();
        this.close();
    }

    private void reset() {
        this.nameField.setText("");
        this.itemIdField.setText("");
        clearError();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return errorMessage.isEmpty(); // Don't allow ESC close if there's an error
    }

    @Override
    public void close() {
        if (!errorMessage.isEmpty()) {
            return; // Don't close if there's an error
        }
        super.close();
    }
}