package com.skyblock21.mixin;

import com.skyblock21.events.BlockEvents;
import com.skyblock21.events.EntityEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {

    @Inject(method = "addEntity", at = @At("TAIL"))
    private void onAddEntity(Entity entity, CallbackInfo ci) {
        EntityEvents.SPAWN.invoker().onEntitySpawn(entity, entity.getId());
    }

    @Inject(method = "removeEntity", at = @At("HEAD"))
    private void onRemoveEntity(int entityId, Entity.RemovalReason removalReason, CallbackInfo ci) {
        EntityEvents.REMOVE.invoker().onEntityRemove(entityId);
    }

    @Inject(method = "handleBlockUpdate", at = @At("HEAD"))
    private void onBlockUpdate(BlockPos pos, BlockState state, int flags, CallbackInfo ci) {
        World world = (World) (Object) this;
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
