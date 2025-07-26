package com.skyblock21.mixin;

import com.skyblock21.tracking.BaseTracker;
import com.skyblock21.tracking.TrackerManager;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "stop", at = @At("HEAD"))
    private void onClientStop(CallbackInfo ci) {
        saveAllTrackers();
    }

    @Unique
    private void saveAllTrackers() {
        TrackerManager.getAllTrackers().forEach(BaseTracker::saveAndClose);
    }
}
