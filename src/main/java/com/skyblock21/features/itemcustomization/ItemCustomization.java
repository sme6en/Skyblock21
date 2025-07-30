package com.skyblock21.features.itemcustomization;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import lombok.Getter;
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

    @Getter
    private Item customItem = null;

    public ItemCustomization() {}

    public ItemCustomization(String customName, String customItemId, boolean hasGlint) {
        this.customName = customName;
        this.customItemId = customItemId;
        this.hasGlint = hasGlint;

        if (isValidItemId() && hasCustomItemId()) {
            this.customItem = Registries.ITEM.get(Identifier.tryParse(customItemId));
        }
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
}