package com.skyblock21.mixin;

import com.skyblock21.events.BlockEvents;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

//    @Accessor("world")
//    private ServerWorld getWorldAccessor() {
//        throw new AssertionError();
//    }
//
//    @Accessor("player")
//    private ServerPlayerEntity getPlayerAccessor() {
//        throw new AssertionError();
//    }
//
//    @Inject(method = "tryBreakBlock", at = @At("HEAD"))
//    private void onBlockBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
//        ServerPlayerInteractionManager manager = (ServerPlayerInteractionManager) (Object) this;
//        ServerWorld world = this.getWorldAccessor();
//        ServerPlayerEntity player = this.getPlayerAccessor();
//        BlockState state = world.getBlockState(pos);
//
//        BlockEvents.BREAK_BLOCK.invoker().onBlockBreak(world, pos, state, player);
//    }

}
