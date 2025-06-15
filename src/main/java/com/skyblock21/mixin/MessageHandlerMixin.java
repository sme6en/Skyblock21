package com.skyblock21.mixin;

import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.events.ChatEvents;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Mixin(value = MessageHandler.class, priority = 600)
public class MessageHandlerMixin {

    @Inject(method = "onGameMessage", at = @At("HEAD"))
    private void onGameMessage(Text text, boolean overlay, CallbackInfo ci) {
        if (overlay) return;
        ChatEvents.RECEIVE_TEXT.invoker().onMessage(text);
    }


    @ModifyArgs(method = "onGameMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;)V"))
    private void modifyChatMessage(Args args) {
        if (!Skyblock21ConfigManager.get().general.timestampBeforeMessages) return;

        Text text = args.get(0);
        if (text.getString().trim().isEmpty()) return;

        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        Text timestampedText = Text.literal("§7[" + time + "]§r ").append(text);
        args.set(0, timestampedText);
    }
}
