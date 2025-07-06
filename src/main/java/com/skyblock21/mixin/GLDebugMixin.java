package com.skyblock21.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.client.gl.GlDebug;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlDebug.class)
public class GLDebugMixin {

    private static boolean hasPosted1280 = false;
    private static boolean hasPosted1281 = false;
    private static boolean hasPosted1282 = false;

    @Inject(method = "onDebugMessage", at = @At("HEAD"), cancellable = true)
    private static void suppressMessage(int source, int type, int id, int severity, int length, long message, long l, CallbackInfo ci) {
        if (ci == null) return;

        if (id == 1280) {
            if (hasPosted1280) {
                ci.cancel();
            } else {
                hasPosted1280 = true;
            }
        } else if (id == 1281) {
            if (hasPosted1281) {
                ci.cancel();
            } else {
                hasPosted1281 = true;
            }
        } else if (id == 1282) {
            if (hasPosted1282) {
                ci.cancel();
            } else {
                hasPosted1282 = true;
            }
        }
    }

}
