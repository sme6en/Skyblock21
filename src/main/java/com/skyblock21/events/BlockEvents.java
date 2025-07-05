package com.skyblock21.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockEvents {
    public static final Event<BlockBreakEvent> BREAK_BLOCK = EventFactory.createArrayBacked(BlockBreakEvent.class,
            (listeners) -> (world, pos, state, player) -> {
                for (BlockBreakEvent listener : listeners) {
                    listener.onBlockBreak(world, pos, state, player);
                }
            });

    public interface BlockBreakEvent {
        void onBlockBreak(World world, BlockPos pos, BlockState state, PlayerEntity player);
    }
}