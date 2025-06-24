package com.skyblock21.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ShovelItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ShovelItem.class)
public class ShovelItemMixin {

    private static final List<Block> BLOCK_LIST = List.of(
            Blocks.GRASS_BLOCK,
            Blocks.DIRT,
            Blocks.PODZOL,
            Blocks.COARSE_DIRT,
            Blocks.MYCELIUM,
            Blocks.ROOTED_DIRT
    );
    

    @Inject(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"), cancellable = true)
    private void sb21$preventPathCreation(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);

        // Prevent path creation by returning early if the block is already a path
        if (BLOCK_LIST.contains(blockState.getBlock()) && Utils.isOnSkyblock() && Skyblock21ConfigManager.get().general.preventDirtRoads) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }
}
