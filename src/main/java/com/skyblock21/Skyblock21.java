package com.skyblock21;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.events.ReceiveChatMessageEvent;
import com.skyblock21.features.*;
import com.skyblock21.features.foraging.HOTFOverlay;
import com.skyblock21.features.foraging.TreeProgress;
import com.skyblock21.hud.EditGuiScreen;
import com.skyblock21.hud.HudManager;
import com.skyblock21.hud.elements.TestHudElement;
import com.skyblock21.hud.elements.TreeProgressHudElement;
import com.skyblock21.util.AutoUpdater;
import com.skyblock21.util.Location;
import com.skyblock21.util.Utils;
import com.skyblock21.util.tab.TabUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.util.Objects;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Skyblock21 implements ClientModInitializer {
    public static final String MOD_ID = "skyblock21";
    public static final String MOD_VERSION = "1.0.0";
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();


    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, commandRegistryAccess) -> {
            dispatcher.register(literal("skyblock21").executes((ctx) -> {
                MinecraftClient.getInstance()
                               .send(() -> MinecraftClient.getInstance()
                                                          .setScreen(Skyblock21ConfigManager.createGUI(null)));
                return 1;
            }).then(literal("gui").executes((fabricClientCommandSourceCommandContext) -> {
                MinecraftClient.getInstance()
                        .send(() -> MinecraftClient.getInstance()
                                .setScreen(new EditGuiScreen(MinecraftClient.getInstance().currentScreen)));
                return 1;
            })));
        });

    }

    public void tick(MinecraftClient client) {
        Utils.update();
    }

    @Override
    public void onInitializeClient() {
//		AutoUpdater.onStartup();
//		AutoUpdater.checkForUpdate();

        ClientTickEvents.END_CLIENT_TICK.register(this::tick);

        ClientPlayConnectionEvents.JOIN.register(((clientPlayNetworkHandler, packetSender, minecraftClient) -> {
            if (!Objects.equals(AutoUpdater.latestUpdatedVersion, MOD_VERSION) && AutoUpdater.latestUpdatedVersion != "") {
                minecraftClient.execute(() -> {
                    if (minecraftClient.player == null) return;
                    minecraftClient.player.sendMessage(Text.literal("Skyblock21 updated to version: §b§l" + AutoUpdater.latestUpdatedVersion + "§r! Please restart your game to access new features."), false);
                });
            }
        }));

        Skyblock21ConfigManager.load();
        ReceiveChatMessageEvent.init();
        Utils.init();
        TabUtils.init();
        CookieGodPotReminder.init();
        ScathaAlert.init();
        CopyToClipboardRNG.init();
        MouseLock.init();
        Debug.init();
        TreeProgress.init();
        HOTFOverlay.init();

        HudManager.register(new TestHudElement(100, 120));
        HudManager.register(new TreeProgressHudElement(100, 190));
        HudManager.init();
        registerCommands();

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> Skyblock21ConfigManager.save());
    }
}