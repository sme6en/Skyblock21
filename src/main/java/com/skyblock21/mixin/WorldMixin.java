package com.skyblock21.mixin;

import com.skyblock21.events.BlockEvents;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class WorldMixin {

    @Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z", at = @At("RETURN"))
    private void sb21$onSetBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir) {
        World world = (World) (Object) this;
        if (cir.getReturnValue() && !world.isClient) {
            BlockState oldState = world.getBlockState(pos);
            if (!oldState.isOf(state.getBlock())) {
                if (!state.isAir()) {
                    BlockEvents.BLOCK_ADDED.invoker().onBlockAdded(world, pos, state);
                }
                if (!oldState.isAir()) {
                    BlockEvents.BLOCK_BROKEN.invoker().onBlockBroken(world, pos, oldState);
                }
            }
        }
    }
}