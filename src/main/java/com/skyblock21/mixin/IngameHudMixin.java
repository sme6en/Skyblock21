package com.skyblock21.mixin;

import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.features.itemcustomization.ItemCustomization;
import com.skyblock21.util.TextUtils;
import com.skyblock21.util.Utils;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(InGameHud.class)
public class IngameHudMixin {

    @ModifyArg(
            method = "renderHotbarItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;III)V", ordinal = 0))
    private ItemStack customizeHotbarItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return itemStack;
        }

        String uuid = Utils.getItemUUID(itemStack);
        if (uuid.isEmpty()) {
            return itemStack;
        }

        ItemCustomization customization = PersistentData.get().itemCustomizations.get(uuid);
        if (customization == null) {
            return itemStack;
        }

        if (customization.getCustomItem() == null) {
            return itemStack;
        }

        return customization.getCustomItem().getDefaultStack();
    }

    @ModifyArg(method = "renderHotbarItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawStackOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;II)V", ordinal = 0))
    private ItemStack customizeHotbarItemOverlay(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return itemStack;
        }

        String uuid = Utils.getItemUUID(itemStack);
        if (uuid.isEmpty()) {
            return itemStack;
        }

        ItemCustomization customization = PersistentData.get().itemCustomizations.get(uuid);
        if (customization == null) {
            return itemStack;
        }

        if (customization.getCustomItem() == null) {
            return itemStack;
        }

        return customization.getCustomItem().getDefaultStack();
    }
}

