package com.skyblock21.mixin;

import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.features.HideAroundNPC;
import com.skyblock21.util.Location;
import com.skyblock21.util.Utils;
import net.minecraft.client.render.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.client.network.OtherClientPlayerEntity;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void shouldRender(Entity entity, net.minecraft.client.render.Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (Utils.isOnSkyblock()) {
            if (mc.world != null && Skyblock21ConfigManager.get().general.hidePlayersAroundNpcs &&
                    Utils.getLocation() != Location.PRIVATE_ISLAND && !Utils.isInDungeons()) {
                if (entity instanceof OtherClientPlayerEntity && !HideAroundNPC.isNPC(entity) && HideAroundNPC.isNearNPC(entity)) {
                    cir.setReturnValue(false);
                }
            }
        }
    }

}
