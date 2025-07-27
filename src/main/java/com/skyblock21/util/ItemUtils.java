package com.skyblock21.util;

import net.minecraft.component.ComponentHolder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class ItemUtils {

    @SuppressWarnings("deprecation")
    public static NbtCompound getCustomData(ComponentHolder componentHolder) {
        return componentHolder.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).getNbt();
    }

    public static String getItemUUID(ComponentHolder stack) {
        return getCustomData(stack).getString("uuid").orElse("");
    }

    public static String getItemId(ComponentHolder stack) {
        return getCustomData(stack).getString("id").orElse("");

    }
}
