package com.skyblock21.events;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.text.Text;

@FunctionalInterface
public interface ReceiveChatMessageEvent {

    Event<ReceiveChatMessageEvent> EVENT = EventFactory.createArrayBacked(ReceiveChatMessageEvent.class,
            listeners -> (message, overlay, cancelled) -> {
                for (ReceiveChatMessageEvent listener : listeners) {
                    listener.onMessage(message, overlay, cancelled);
                }
            });

    void onMessage(Text message, boolean overlay, boolean cancelled);

    static void init() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> EVENT.invoker().onMessage(message, overlay, false));
        ClientReceiveMessageEvents.GAME_CANCELED.register((message, overlay) -> EVENT.invoker().onMessage(message, overlay, true));
    }

}
