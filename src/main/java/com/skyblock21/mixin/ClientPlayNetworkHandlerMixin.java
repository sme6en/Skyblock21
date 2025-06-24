package com.skyblock21.mixin;

import com.skyblock21.features.commandaliases.CommandAliases;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "sendChatCommand", at = @At("HEAD"), cancellable = true)
    private void processCommandAliases(String command, CallbackInfo ci) {
        String processedCommand = CommandAliases.processCommand("/" + command);

        if (processedCommand != null) {
            ci.cancel();

            ClientPlayNetworkHandler handler = (ClientPlayNetworkHandler) (Object) this;

            String commandWithoutSlash = processedCommand.substring(1);
            handler.sendChatCommand(commandWithoutSlash);
        }
    }
}
