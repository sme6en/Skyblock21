package com.skyblock21.mixin.accessors;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ChatHud.class)
public interface ChatHudAccessor {

    @Accessor("messages")
    List<ChatHudLine> getMessages();
}
