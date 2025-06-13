package com.skyblock21.mixin;

import com.skyblock21.Skyblock21;
import com.skyblock21.config.Skyblock21Config;
import com.skyblock21.config.Skyblock21ConfigManager;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {

    @ModifyArgs(method = "applyFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Fog;<init>(FFLnet/minecraft/client/render/FogShape;FFFF)V"))
    private static void modifyFogDistance(Args args, Camera camera, BackgroundRenderer.FogType fogType, Vector4f color, float viewDistance, boolean thickenFog, float tickDelta) {
        if (fogType == BackgroundRenderer.FogType.FOG_TERRAIN) {
            if (Skyblock21ConfigManager.get().general.noFog) {
                args.set(0, viewDistance * 4);
                args.set(1, viewDistance * 4.25f);
            }
        }
    }
}
