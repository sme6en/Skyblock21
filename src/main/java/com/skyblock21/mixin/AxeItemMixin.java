package com.skyblock21.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.skyblock21.Skyblock21;
import com.skyblock21.config.Skyblock21Config;
import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.util.Utils;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemUsageContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(AxeItem.class)
public class AxeItemMixin {

    @ModifyReturnValue(method = "shouldCancelStripAttempt", at = @At("RETURN"))
    private static boolean sb21$shouldCancelStripAttempt(boolean original) {

        if (Utils.isOnSkyblock() && Skyblock21ConfigManager.get().foraging.preventLogStripping) {
            return true;
        }

        return original;
    }
}
