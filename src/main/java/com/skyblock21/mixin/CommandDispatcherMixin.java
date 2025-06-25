package com.skyblock21.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.features.commandaliases.Alias;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Mixin(CommandDispatcher.class)
public class CommandDispatcherMixin<S> {

    @Shadow(remap = false) @Final
    private RootCommandNode<S> root;

    @Inject(
            method = "getCompletionSuggestions*",
            at = @At("RETURN"),
            remap = false
    )
    private void refreshAliases(CallbackInfoReturnable<?> cir) {
        refreshAllAliases();
    }

    @Unique
    private void registerAliasCommand(String aliasName, LiteralCommandNode<S> originalCommand) {
        CommandNode<S> existingAlias = root.getChild(aliasName);
        if (existingAlias != null) {
            return;
        }

        // Create a new literal command node for the alias
        LiteralArgumentBuilder<S> aliasBuilder = LiteralArgumentBuilder.<S>literal(aliasName);

        // Copy the command execution from the original
        if (originalCommand.getCommand() != null) {
            aliasBuilder.executes(originalCommand.getCommand());
        }

        // Set redirect to the original command to inherit all children/arguments
        aliasBuilder.redirect(originalCommand);

        // Build and add the alias command
        LiteralCommandNode<S> aliasCommand = aliasBuilder.build();
        root.addChild(aliasCommand);
    }

    @Unique
    public void refreshAllAliases() {
        // Remove old aliases that are no longer enabled
        Collection<CommandNode<S>> children = root.getChildren();
        children.removeIf(node -> {
            if (node instanceof LiteralCommandNode<S> literalNode) {
                String nodeName = literalNode.getLiteral();

                return PersistentData.get().aliases.stream()
                                                   .anyMatch(alias -> alias.aliasCommand.equals(nodeName) && !alias.enabled);
            }
            return false;
        });

        // Add new aliases
        for (Alias alias : PersistentData.get().aliases) {
            if (alias.enabled) {
                CommandNode<S> targetCommand = root.getChild(alias.targetCommand);
                if (targetCommand instanceof LiteralCommandNode<S> literalTarget) {
                    CommandNode<S> existingAlias = root.getChild(alias.aliasCommand);
                    if (existingAlias == null) {
                        registerAliasCommand(alias.aliasCommand, literalTarget);
                    }
                }
            }
        }
    }

}

