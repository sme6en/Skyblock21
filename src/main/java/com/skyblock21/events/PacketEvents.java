package com.skyblock21.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.network.packet.Packet;


public class PacketEvents {

    public static final Event<PacketReceived> RECEIVED = EventFactory.createArrayBacked(PacketReceived.class, (listeners) -> (packet) -> {
        for (PacketReceived listener : listeners) {
            boolean result = listener.onPacketReceived(packet);

            if (!result) {
                return false;
            }

        }
        return true;
    });

    public static final Event<PacketSent> SENT = EventFactory.createArrayBacked(PacketSent.class, (listeners) -> (packet) -> {
        for (PacketSent listener : listeners) {
            boolean result = listener.onPacketSent(packet);

            if (!result) {
                return false;
            }
        }

        return true;
    });

    @FunctionalInterface
    public interface PacketReceived {
        boolean onPacketReceived(Packet<?> packet);
    }

    @FunctionalInterface
    public interface PacketSent {
        boolean onPacketSent(Packet<?> packet);
    }

}
