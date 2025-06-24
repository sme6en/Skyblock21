package com.skyblock21.mixin;

import com.skyblock21.events.PlayerEvents;
import com.skyblock21.util.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(method = "dropItem",
            at = @At("HEAD"),
            cancellable = true)
    private void onDropItem(ItemStack stack, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
        if (!Utils.isOnSkyblock()) return;
        if (MinecraftClient.getInstance().player == null) {
            return;
        }

        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        ItemStack currentStack = player.getMainHandStack();

        ActionResult result = PlayerEvents.DROP_ITEM.invoker()
                                                .interact(player, currentStack);

        if (result == ActionResult.FAIL) {
            cir.setReturnValue(null);
        }
    }
}
