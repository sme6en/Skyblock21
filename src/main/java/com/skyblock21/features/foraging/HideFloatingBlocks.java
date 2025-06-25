package com.skyblock21.features.foraging;

import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;

import java.util.Arrays;

public class HideFloatingBlocks {

    private static final Block[] VALID_BLOCKS = {
            Blocks.STRIPPED_SPRUCE_WOOD,
            Blocks.AZALEA_LEAVES,
            Blocks.MANGROVE_WOOD,
            Blocks.MANGROVE_LEAVES
    };

    public static boolean shouldHideFloatingBlocks(Entity entity) {
        if (!(entity instanceof DisplayEntity.BlockDisplayEntity blockDisplayEntity)) return false;

        boolean validBlock = Arrays.stream(VALID_BLOCKS).
                anyMatch(block -> block.equals(blockDisplayEntity.getBlockState().getBlock()));

        return Skyblock21ConfigManager.get().foraging.hideFloatingBlocks && Utils.isInGalatea() && validBlock;
    }

}
