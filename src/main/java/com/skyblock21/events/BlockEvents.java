package com.skyblock21.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockEvents {

    public static final Event<BlockAdded> BLOCK_ADDED = EventFactory.createArrayBacked(
            BlockAdded.class,
            (listeners) -> (world, pos, state) -> {
                for (BlockAdded listener : listeners) {
                    listener.onBlockAdded(world, pos, state);
                }
            }
    );

    public static final Event<BlockBroken> BLOCK_BROKEN = EventFactory.createArrayBacked(
            BlockBroken.class,
            (listeners) -> (world, pos, state) -> {
                for (BlockBroken listener : listeners) {
                    listener.onBlockBroken(world, pos, state);
                }
            }
    );

    @FunctionalInterface
    public interface BlockAdded {
        void onBlockAdded(World world, BlockPos pos, BlockState state);
    }

    @FunctionalInterface
    public interface BlockBroken {
        void onBlockBroken(World world, BlockPos pos, BlockState state);
    }
}