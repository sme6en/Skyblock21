package com.skyblock21.events;

import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

public class PlayerEvents {

    public static Event<PlayerItemDropEvent> DROP_ITEM = EventFactory.createArrayBacked(PlayerItemDropEvent.class,
            (listeners) -> (player, stack) -> {
                for (PlayerItemDropEvent listener : listeners) {
                    ActionResult result = listener.interact(player, stack);
                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }
                return ActionResult.PASS;
            });

    public interface PlayerItemDropEvent {
        ActionResult interact(PlayerEntity player, ItemStack stack);
    }
}
