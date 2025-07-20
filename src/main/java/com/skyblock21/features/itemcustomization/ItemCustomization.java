package com.skyblock21.features.itemcustomization;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ItemCustomization {

    @SerialEntry
    public String customName = "";

    @SerialEntry
    public String customItemId = "";

    @SerialEntry
    public boolean hasGlint = false;

    public ItemCustomization() {}

    public ItemCustomization(String customName, String customItemId, boolean hasGlint) {
        this.customName = customName;
        this.customItemId = customItemId;
        this.hasGlint = hasGlint;
    }

    public boolean hasCustomName() {
        return customName != null && !customName.isEmpty();
    }

    public boolean hasCustomItemId() {
        return customItemId != null && !customItemId.isEmpty();
    }

    public boolean isValidItemId() {
        if (!hasCustomItemId()) return true;

        try {
            Identifier id = Identifier.tryParse(customItemId);
            if (id == null) return false;

            Item item = Registries.ITEM.get(id);
            return item != null && !item.equals(Registries.ITEM.get(Identifier.of("air")));
        } catch (Exception e) {
            return false;
        }
    }

    public Item getCustomItem() {
        if (!hasCustomItemId() || !isValidItemId()) return null;
        return Registries.ITEM.get(Identifier.tryParse(customItemId));
    }
}