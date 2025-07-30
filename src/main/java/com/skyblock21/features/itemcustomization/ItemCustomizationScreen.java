package com.skyblock21.features.itemcustomization;

import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.gui.Theme;
import com.skyblock21.gui.ThemeManager;
import com.skyblock21.gui.components.*;
import com.skyblock21.util.ItemUtils;
import com.skyblock21.util.TextUtils;
import com.skyblock21.util.TickSchedulerHelper;
import com.skyblock21.util.Utils;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import  net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import net.minecraft.registry.Registries;

import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;

import static net.minecraft.text.Text.literal;

public class ItemCustomizationScreen extends BaseOwoScreen<FlowLayout> {

    private final ItemStack itemStack;
    private final String itemUuid;
    private ItemCustomization customization;

    private TextBox itemNameComponent;
    private Checkbox glintComponent;
    private TextBox itemIdComponent;
    private ItemComponent preview;

    private static final String[][] COLOR_CODES = {
            {"&0", "Black", "0x000000"},
            {"&1", "Dark Blue", "0x0000AA"},
            {"&2", "Dark Green", "0x00AA00"},
            {"&3", "Dark Aqua", "0x00AAAA"},
            {"&4", "Dark Red", "0xAA0000"},
            {"&5", "Dark Purple", "0xAA00AA"},
            {"&6", "Gold", "0xFFAA00"},
            {"&7", "Gray", "0xAAAAAA"},
            {"&8", "Dark Gray", "0x555555"},
            {"&9", "Blue", "0x5555FF"},
            {"&a", "Green", "0x55FF55"},
            {"&b", "Aqua", "0x55FFFF"},
            {"&c", "Red", "0xFF5555"},
            {"&d", "Light Purple", "0xFF55FF"},
            {"&e", "Yellow", "0xFFFF55"},
            {"&f", "White", "0xFFFFFF"}
    };

    private static final String[][] FORMAT_CODES = {
            {"&k", "Obfuscated", "Random chars"},
            {"&l", "Bold", "Bold text"},
            {"&m", "Strikethrough", "Crossed out"},
            {"&n", "Underline", "Underlined"},
            {"&o", "Italic", "Italic text"},
            {"&r", "Reset", "Reset formatting"}
    };

    private static Animation animation;

    public ItemCustomizationScreen(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.itemUuid = ItemUtils.getItemUUID(itemStack);

        this.customization = PersistentData.get().itemCustomizations.getOrDefault(
                itemUuid,
                new ItemCustomization()
        );
        String name = this.customization.customName.isEmpty() ? (itemStack.getCustomName() != null ? TextUtils.translateColorCodes(TextUtils.toLegacy(itemStack.getCustomName()), true) : "") : this.customization.customName;

        itemNameComponent = new TextBox(Sizing.fixed(210), literal("Item name"));
        itemNameComponent.setMaxLength(200);
        itemNameComponent.setText(name);
        itemNameComponent.setPreviewColors(true);
        glintComponent = (Checkbox) new Checkbox(literal("Glint")).checked(customization.hasGlint);
        itemIdComponent = new TextBox(Sizing.fixed(150), literal("ID"));
        itemIdComponent.setText(customization.customItemId.isEmpty() ? Registries.ITEM.getId(itemStack.getItem()).getPath() : customization.customItemId);
        itemIdComponent.onChanged().subscribe((s) -> {
            ItemStack newPreviewStack = createPreviewStack();
            preview.stack(newPreviewStack);
        });
        itemNameComponent.onChanged().subscribe((s) -> {
            ItemStack newPreviewStack = createPreviewStack();
            preview.stack(newPreviewStack);
        });
        glintComponent.onChanged((s) -> {
            ItemStack newPreviewStack = createPreviewStack();
            preview.stack(newPreviewStack);
        });
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::horizontalFlow);
    }

    @Override
    public void init() {
        super.init();
        animation.forwards();
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        ItemStack previewStack = createPreviewStack();
        Theme theme = ThemeManager.getCurrentTheme();

        preview = Components.item(previewStack);
        preview.sizing(Sizing.fixed(80));
        preview.setTooltipFromStack(true);
        preview.margins(Insets.both(10, 10));

        FlowLayout idGlintContainer = Containers.horizontalFlow(Sizing.content(), Sizing.content()).gap(6);
        idGlintContainer.child(itemIdComponent).child(glintComponent);

        rootComponent.horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER);

        FlowLayout container = (FlowLayout) Containers.horizontalFlow(Sizing.fixed(0), Sizing.fill())
                                                      .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        animation = container.horizontalSizing().animate(500, Easing.CUBIC, Sizing.fill());

        rootComponent.child(container);

        FlowLayout buttonsContainer = Containers.horizontalFlow(Sizing.content(), Sizing.content()).gap(6);
        buttonsContainer.child(new Button(literal("Apply"), theme.primary, theme.background, button -> {
                            save();
                        }).textShadow(false).horizontalSizing(Sizing.content(6)))
                        .child(new Button(literal("Reset"), theme.getSecondaryBackground(), theme.text, button -> {
                            reset();
                        }).textShadow(false).horizontalSizing(Sizing.content(6)))
                        .child(new Button(literal("Cancel"), theme.getSecondaryBackground(), theme.text, button -> {
                            close();
                        }).textShadow(false).horizontalSizing(Sizing.content(6)));

        container.child(new RoundedContainer(Sizing.content(), Sizing.content(), theme.getRounding(), theme.background, FlowLayout.Algorithm.VERTICAL).gap(8).child(preview)
                                                                                                                                                      .child(itemNameComponent)
                                                                                                                                                      .child(idGlintContainer)
                                                                                                                                                      .child(buttonsContainer)
                        .surface(Surface.VANILLA_TRANSLUCENT)
                        .padding(Insets.both(15, 15))
                        .horizontalAlignment(HorizontalAlignment.CENTER)
                        .verticalAlignment(VerticalAlignment.CENTER)
        );

        FlowLayout stylingContainer = new RoundedContainer(Sizing.content(), Sizing.content(), theme.getRounding(), theme.background, FlowLayout.Algorithm.VERTICAL).gap(5);
        stylingContainer.surface(Surface.VANILLA_TRANSLUCENT);
        stylingContainer.padding(Insets.both(15, 15));


        for (String[] colorCode : COLOR_CODES) {
            String code = colorCode[0];
            String name = colorCode[1];
            String formattedCode = code.replace('&', '§');

            stylingContainer.child(new Label(literal(formattedCode + code + " " + name)).shadow(true));
        }

        for (String[] formatCode : FORMAT_CODES) {
            String code = formatCode[0];
            String name = formatCode[1];

            stylingContainer.child(new Label(literal(code + " " + name)).shadow(true));
        }

        container.child(Components.spacer(2));
        container.child(stylingContainer);
        rootComponent.surface(Surface.blur(3, 10));
    }

    private ItemStack createPreviewStack() {
        ItemStack preview = this.itemStack.copy();

        String customItemId = this.itemIdComponent.getText().trim();
        if (!customItemId.isEmpty()) {
            try {
                Identifier id = Identifier.tryParse(customItemId);
                if (id != null) {
                    Item customItem = Registries.ITEM.get(id);
                    if (customItem != null && !customItem.equals(Registries.ITEM.get(Identifier.of("air")))) {
                        preview = new ItemStack(customItem, preview.getCount());
                        preview.set(DataComponentTypes.CUSTOM_DATA, this.itemStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT));

                        LoreComponent lore = this.itemStack.get(DataComponentTypes.LORE);
                        if (lore != null) {
                            preview.set(DataComponentTypes.LORE, lore);
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        String customName = this.itemNameComponent.getText().trim();
        if (!customName.isEmpty()) {
            String formattedName = TextUtils.translateColorCodes(customName, false);
            preview.set(DataComponentTypes.CUSTOM_NAME, Text.literal(formattedName));
        }

        if (this.glintComponent.isChecked()) {
            preview.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        } else {
            preview.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false);
        }

        return preview;
    }

    private void save() {
        String itemId = this.itemIdComponent.getText().trim();
        if (!itemId.isEmpty()) {
            ItemCustomization temp = new ItemCustomization("", itemId, false);
            if (!temp.isValidItemId()) {
                itemId = Registries.ITEM.getId(this.itemStack.getItem()).getPath();
            }
        }

        ItemCustomization newCustomization = new ItemCustomization(
                this.itemNameComponent.getText(),
                itemId,
                this.glintComponent.isChecked()
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
        this.itemNameComponent.setText(itemStack.getCustomName() != null ? TextUtils.translateColorCodes(TextUtils.toLegacy(itemStack.getCustomName()), true) : "");
        this.itemIdComponent.setText(Registries.ITEM.getId(this.itemStack.getItem()).getPath());
    }

    @Override
    public void close() {
        animation.backwards();
        TickSchedulerHelper.runAfter(super::close, 10);
    }

}
