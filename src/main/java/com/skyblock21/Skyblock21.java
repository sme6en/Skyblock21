package com.skyblock21;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.config.Skyblock21Screen;
import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.events.SkyblockEvents;
import com.skyblock21.features.*;
import com.skyblock21.features.commandaliases.CommandAliasesScreen;
import com.skyblock21.features.foraging.GalateaTracker;
import com.skyblock21.features.foraging.HOTFOverlay;
import com.skyblock21.features.foraging.TreeProgress;
import com.skyblock21.features.items.StarredDropPrevention;
import com.skyblock21.features.keyshortcuts.KeyShortcuts;
import com.skyblock21.features.keyshortcuts.KeyShortcutsScreen;
import com.skyblock21.features.kuudra.Kuudra;
import com.skyblock21.hud.EditGuiScreen;
import com.skyblock21.hud.HudElement;
import com.skyblock21.hud.HudManager;
import com.skyblock21.hud.elements.*;
import com.skyblock21.util.AutoUpdater;
import com.skyblock21.util.TextUtils;
import com.skyblock21.util.Utils;
import com.skyblock21.util.tab.TabUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Skyblock21 implements ClientModInitializer {
    public static final String MOD_ID = "skyblock21";
    public static final String MOD_VERSION = "1.2.2.1";
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();


    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registry) -> {
            dispatcher.register(literal("skyblock21").executes((ctx) -> {
                MinecraftClient.getInstance()
                               .send(() -> MinecraftClient.getInstance()
                                                          .setScreen(new Skyblock21Screen()));
                return 1;
            }).then(literal("config").executes((ctx) -> {
                MinecraftClient.getInstance()
                               .send(() -> MinecraftClient.getInstance()
                                                          .setScreen(Skyblock21ConfigManager.createGUI(null)));
                return 1;
            })).then(literal("gui").executes((ctx) -> {
                MinecraftClient.getInstance()
                               .send(() -> MinecraftClient.getInstance()
                                                          .setScreen(new EditGuiScreen(MinecraftClient.getInstance().currentScreen)));
                return 1;
            })).then(literal("keys").executes((ctx) -> {
                MinecraftClient.getInstance()
                               .send(() -> MinecraftClient.getInstance()
                                                          .setScreen(new KeyShortcutsScreen(MinecraftClient.getInstance().currentScreen)));
                return 1;
            })).then(literal("aliases").executes((ctx) -> {
                MinecraftClient.getInstance()
                               .send(() -> MinecraftClient.getInstance()
                                                          .setScreen(new CommandAliasesScreen(MinecraftClient.getInstance().currentScreen)));
                return 1;
            })));
        });

    }

    public void tick(MinecraftClient client) {
        Utils.update();
    }

    @Override
    public void onInitializeClient() {
        AutoUpdater.checkForUpdate();
        AutoUpdater.onStartup();

        ClientTickEvents.END_CLIENT_TICK.register(this::tick);

        ClientPlayConnectionEvents.JOIN.register(((clientPlayNetworkHandler, packetSender, minecraftClient) -> {
            if (!Objects.equals(AutoUpdater.latestUpdatedVersion, MOD_VERSION) && AutoUpdater.latestUpdatedVersion != "") {
                minecraftClient.execute(() -> {
                    if (minecraftClient.player == null) return;
                    TextUtils.addMessage("Updated to version: §b§l" + AutoUpdater.latestUpdatedVersion + "§r! Please restart your game to access new features.", true, false);
                });
            }
        }));

        // General
        Skyblock21ConfigManager.load();
        PersistentData.init();
        Utils.init();
        TabUtils.init();
//        Debug.init();

        // Misc
        CookieGodPotReminder.init();
        CopyToClipboardRNG.init();
        MouseLock.init();
        HideAroundNPC.init();
        KeyShortcuts.init();

        // Mining
        Scathas.init();

        // Galatea
        TreeProgress.init();
        HOTFOverlay.init();
        GalateaTracker.init();

        // Items
        StarredDropPrevention.init();

        // Kuudra
        Kuudra.init();

        HudManager.register(new TestHudElement(100, 50));
        HudManager.register(new TreeProgressHudElement(100, 190));
        HudManager.register(new GalateaTrackerElement(10, 10));
        HudManager.register(new BonusGiftsTrackerElement(10, 40));
        HudManager.register(new ScathaTrackerElement(10, 60));
        HudManager.init();
        registerCommands();

        SkyblockEvents.LOCATION_CHANGE.register((location -> {
            PersistentData.save();
        }));

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            Skyblock21ConfigManager.save();
            PersistentData.save();
        });
    }
}