package com.skyblock21.mixin;

import com.skyblock21.events.PlayerEvents;
import com.skyblock21.features.commandaliases.CommandAliases;
import com.skyblock21.util.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "dropSelectedItem",
            at = @At("HEAD"),
            cancellable = true)
    private void onDropItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        if (!Utils.isOnSkyblock()) {
            return;
        }

        if (MinecraftClient.getInstance().player == null) {
            return;
        }

        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        ItemStack currentStack = player.getMainHandStack();

        // Fire the custom event
        ActionResult result = PlayerEvents.DROP_ITEM.invoker()
                                                .interact(player, currentStack);

        // If the event returns FAIL, cancel the drop
        if (result == ActionResult.FAIL) {
            cir.setReturnValue(null);
        }
    }
}
