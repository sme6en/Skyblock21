package com.skyblock21.mixin;

import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.features.itemcustomization.ItemCustomization;
import com.skyblock21.util.Utils;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

    @ModifyArg(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V", ordinal = 0))
    private ItemStack customizeHeldItem(ItemStack itemStack) {
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
