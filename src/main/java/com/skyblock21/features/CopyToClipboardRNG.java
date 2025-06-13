package com.skyblock21.features;

import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.events.ReceiveChatMessageEvent;
import com.skyblock21.util.TextUtils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;

public class CopyToClipboardRNG {

    public static String[] MESSAGE_PREFIXES = {
            "§r§9§lVERY RARE DROP! ",
            "§r§5§lVERY RARE DROP! ",
            "§r§d§lCRAZY RARE DROP! ",
            "§r§c§lINSANE DROP! ",
            "§r§6§lPET DROP! "
    };

    public static void init() {
        ReceiveChatMessageEvent.EVENT.register((text, overlay, cancelled) -> {
            if (!Skyblock21ConfigManager.get().general.copyToClipboardRNGs) return;
            // if the text starts with any of the MESSAGE_PREFIXES, copy it to clipboard, make it normal not if else everything
            for (String prefix : MESSAGE_PREFIXES) {
                if (text.getString().startsWith(prefix)) {
                    String message = text.getString();
                    MinecraftClient.getInstance().keyboard.setClipboard(message);
                    TextUtils.addMessage("RNG message copied to clipboard: ", true, true);
                    return;
                }
            }
        });
    }

}
