package com.skyblock21.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ParticleEvents {
    public static final Event<ParticleEvents.ParticleSpawnEvent> SPAWN = EventFactory.createArrayBacked(ParticleEvents.ParticleSpawnEvent.class,
            (listeners) -> (packet) -> {
                for (ParticleEvents.ParticleSpawnEvent listener : listeners) {
                    listener.onParticleSpawn(packet);
                }
            });

    public interface ParticleSpawnEvent {
        void onParticleSpawn(ParticleS2CPacket packet);
    }
}
