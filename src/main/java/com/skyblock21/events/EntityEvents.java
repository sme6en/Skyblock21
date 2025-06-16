package com.skyblock21.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;

@Environment(EnvType.CLIENT)
public class EntityEvents {

    public static Event<EntitySpawnEvent> SPAWN = EventFactory.createArrayBacked(EntitySpawnEvent.class,
            (listeners) -> (entity, entityId) -> {
                for (EntitySpawnEvent listener : listeners) {
                    listener.onEntitySpawn(entity, entityId);
                }
            });

    public static Event<EntityRemoveEvent> REMOVE = EventFactory.createArrayBacked(EntityRemoveEvent.class,
            (listeners) -> (entityId) -> {
                for (EntityRemoveEvent listener : listeners) {
                    listener.onEntityRemove(entityId);
                }
            });

    @FunctionalInterface
    public interface EntitySpawnEvent {

        /**
         * Called when an entity spawns.
         *
         * @param entity   The entity that spawned.
         * @param entityId The ID of the spawned entity.
         */
        void onEntitySpawn(Entity entity, int entityId);
    }

    @FunctionalInterface
    public interface EntityRemoveEvent {


        void onEntityRemove(int entityId);
    }
}
