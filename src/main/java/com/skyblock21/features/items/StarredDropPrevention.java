package com.skyblock21.features.items;

import com.skyblock21.events.PlayerEvents;
import com.skyblock21.events.WindowEvents;
import com.skyblock21.util.TextUtils;
import com.skyblock21.util.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;

public class StarredDropPrevention {

    public static void init() {
        PlayerEvents.DROP_ITEM.register(StarredDropPrevention::onDrop);
        WindowEvents.CLOSE_WINDOW.register(StarredDropPrevention::onWindowClose);
    }

    private static boolean onWindowClose(HandledScreen<?> handledScreen, ScreenHandler screenHandler) {
        if (!Utils.isOnSkyblock()) return true;

        ItemStack currentStack = handledScreen.getScreenHandler().getCursorStack();
        if (currentStack == null || currentStack.isEmpty()) {
            return true;
        }

        String name = currentStack.getName().getString();
        if (name.contains("✪")) {
            MinecraftClient.getInstance()
                    .getSoundManager()
                    .play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 1.0f, 1.0f), 1);
            TextUtils.addMessage("§cStarred item drop prevented!", true, false);
            return false;
        }

        return true;
    }


    private static ActionResult onDrop(PlayerEntity playerEntity, ItemStack itemStack) {
        if (!Utils.isOnSkyblock()) return ActionResult.PASS;

        if (itemStack == null || itemStack.isEmpty()) {
            return ActionResult.PASS;
        }

        String name = itemStack.getName().getString();
        if (name.contains("✪") && !Utils.isInDungeons()) {
            MinecraftClient.getInstance()
                    .getSoundManager()
                    .play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 1.0f, 1.0f), 1);
            TextUtils.addMessage("§cStarred item drop prevented!", true, false);
            return ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }
}
