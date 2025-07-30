package com.skyblock21.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.skyblock21.Skyblock21;
import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.features.MouseLock;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;


@Mixin(Mouse.class)
public abstract class MouseMixin {

    @WrapWithCondition(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"))
    private boolean sb21$allowMouseMove(ClientPlayerEntity instance, double v, double v2) {
        return !MouseLock.isMouseLocked;
    }
}
