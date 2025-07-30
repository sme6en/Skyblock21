package com.skyblock21.mixin;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.features.commandaliases.Alias;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(ChatInputSuggestor.class)
public class ChatInputSuggestorMixin {

    @Final
    @Shadow
    private TextFieldWidget textField;

    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    public void show(boolean narrateFirstSuggestion) {

    }

    @Inject(method = "refresh()V", at = @At("HEAD"))
    private void sb21$injectAliasSuggestions(CallbackInfo ci) {
        String text = textField.getText();
        int cursor = textField.getCursor();

        if (text.isEmpty() || cursor == 0 || !text.startsWith("/")) {
            return;
        }

        String textBeforeCursor = text.substring(0, cursor);
        String command = textBeforeCursor.substring(1);

        if (command.contains(" ")) {
            return;
        }

        var matchingAliases = PersistentData.get().aliases.stream()
                                                          .filter(alias -> alias.enabled)
                                                          .filter(alias -> alias.aliasCommand.toLowerCase().startsWith(command.toLowerCase()))
                                                          .toList();

        if (matchingAliases.isEmpty()) {
            return;
        }

        SuggestionsBuilder builder = new SuggestionsBuilder(textBeforeCursor, 1);

        for (Alias alias : matchingAliases) {
            builder.suggest(alias.aliasCommand + " ");
        }

        pendingSuggestions = builder.buildFuture();
        show(false);

    }
}
