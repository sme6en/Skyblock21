package com.skyblock21.features.itemcustomization;

import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.gui.Theme;
import com.skyblock21.gui.ThemeManager;
import com.skyblock21.gui.components.Button;
import com.skyblock21.gui.components.ItemTooltipPreview;
import com.skyblock21.gui.components.RoundedContainer;
import com.skyblock21.util.TextUtils;
import com.skyblock21.util.Utils;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.core.Insets;
import me.x150.renderer.util.Color;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

import static net.minecraft.text.Text.literal;

public class ItemCustomizationScreenV2 extends BaseOwoScreen<FlowLayout> {

    private final ItemStack itemStack;
    private final String itemUuid;
    private ItemCustomization customization;

    private TextBoxComponent itemNameComponent;

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

    public ItemCustomizationScreenV2(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.itemUuid = Utils.getItemUUID(itemStack);

        this.customization = PersistentData.get().itemCustomizations.getOrDefault(
                itemUuid,
                new ItemCustomization()
        );

        ThemeManager.setTheme(Theme.WHITE);
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::horizontalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        ItemStack previewStack = createPreviewStack();

        ItemComponent preview = Components.item(previewStack);
        preview.sizing(Sizing.fixed(80));
        preview.setTooltipFromStack(true);
        preview.margins(Insets.both(10, 10));

        itemNameComponent = Components.textBox(Sizing.fixed(210));

        rootComponent.horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER);

        FlowLayout container = (FlowLayout) Containers.horizontalFlow(Sizing.fill(), Sizing.fill()).alignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP);

        rootComponent.child(container);

        container.child(
                new ItemTooltipPreview(previewStack)
        ).child(Components.spacer(2)).gap(30).child(
                new RoundedContainer(Sizing.content(), Sizing.content(), 16, new Color(255,1,255,255), FlowLayout.Algorithm.VERTICAL)
                        .child(preview)
                        .child(
                                new Button(
                                        literal("Apply"), button -> {
                                    TextUtils.addMessage("hi", true, false);
                                }).textShadow(false))
                        .child(
                                new Button(
                                        literal("Reset"), button -> {
                                    TextUtils.addMessage("hi", true, false);
                                }).textShadow(false))
                        .child(
                                new Button(
                                        literal("Cancel"), button -> {
                                    TextUtils.addMessage("hi", true, false);
                                }).textShadow(false))
                        .surface(Surface.VANILLA_TRANSLUCENT)
                        .padding(Insets.both(15, 15))
                        .horizontalAlignment(HorizontalAlignment.CENTER)
                        .verticalAlignment(VerticalAlignment.CENTER)
        );

        FlowLayout stylingContainer = new RoundedContainer(Sizing.content(), Sizing.content(), 10, new Color(255,255,0,255), FlowLayout.Algorithm.VERTICAL).gap(5);
        stylingContainer.surface(Surface.VANILLA_TRANSLUCENT);
        stylingContainer.padding(Insets.both(30, 30));

        for (String[] colorCode : COLOR_CODES) {
            String code = colorCode[0];
            String name = colorCode[1];
            String formattedCode = code.replace('&', '§');

            stylingContainer.child(Components.label(literal(formattedCode + code + " " + name)));
        }

        container.child(Components.spacer(2));
        container.child(stylingContainer);
        rootComponent.surface(Surface.blur(3, 10));
    }

    private ItemStack createPreviewStack() {
        ItemStack preview = this.itemStack.copy();

//         Apply custom item type if valid
//        String customItemId = this.itemIdField.getText().trim();
//        if (!customItemId.isEmpty()) {
//            try {
//                Identifier id = Identifier.tryParse(customItemId);
//                if (id != null) {
//                    Item customItem = Registries.ITEM.get(id);
//                    if (customItem != null && !customItem.equals(Registries.ITEM.get(Identifier.of("air")))) {
//                        preview = new ItemStack(customItem, preview.getCount());
//                        preview.set(DataComponentTypes.CUSTOM_DATA, this.itemStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT));
//
//                        LoreComponent lore = this.itemStack.get(DataComponentTypes.LORE);
//                        if (lore != null) {
//                            preview.set(DataComponentTypes.LORE, lore);
//                        }
//                    }
//                }
//            } catch (Exception ignored) {}
//        }

//        String customName = this.nameField.getText().trim();
//        if (!customName.isEmpty()) {
//            String formattedName = TextUtils.translateColorCodes(customName, false);
//            preview.set(DataComponentTypes.CUSTOM_NAME, Text.literal(formattedName));
//        }

//        if (this.glintCheckbox.isChecked()) {
//            preview.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
//        } else {
//            preview.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false);
//        }

        return preview;
    }
}
