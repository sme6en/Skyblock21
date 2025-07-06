package com.skyblock21.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.chunk.WorldChunk;

public class ChunkEvents {

    public static final Event<ChunkSpawned> CHUNK_SPAWNED = EventFactory.createArrayBacked(
            ChunkSpawned.class,
            (listeners) -> (world, chunk) -> {
                for (ChunkSpawned listener : listeners) {
                    listener.onChunkSpawned(world, chunk);
                }
            }
    );

    public static final Event<ChunkRemoved> CHUNK_REMOVED = EventFactory.createArrayBacked(
            ChunkRemoved.class,
            (listeners) -> (world, chunk) -> {
                for (ChunkRemoved listener : listeners) {
                    listener.onChunkRemoved(world, chunk);
                }
            }
    );

    @FunctionalInterface
    public interface ChunkSpawned {
        void onChunkSpawned(ClientWorld world, WorldChunk chunk);
    }

    @FunctionalInterface
    public interface ChunkRemoved {
        void onChunkRemoved(ClientWorld world, WorldChunk chunk);
    }

}
