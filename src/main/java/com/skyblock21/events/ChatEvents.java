package com.skyblock21.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ChatEvents {

    public static final Event<ChatTextEvent> RECEIVE_TEXT = EventFactory.createArrayBacked(ChatTextEvent.class, listeners -> message -> {
        for (ChatTextEvent listener : listeners) {
            listener.onMessage(message);
        }
    });

    public interface ChatTextEvent {
        void onMessage(Text message);
    }
}
