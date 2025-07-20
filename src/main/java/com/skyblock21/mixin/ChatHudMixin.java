package com.skyblock21.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.skyblock21.config.Skyblock21ConfigManager;
import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatHudMixin {

    @Inject(method = "clear", at = @At("HEAD"), cancellable = true)
    private void onClear(boolean clearHistory, CallbackInfo ci) {
        if (Skyblock21ConfigManager.get().general.infinityChatHistory) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(
            method = {"addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V", "addVisibleMessage"},
            at = @At(value = "CONSTANT", args = "intValue=100")
    )
    private int moreMessages(int hundred) {
        return 16_384;
    }
}
