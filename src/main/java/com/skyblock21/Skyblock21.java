package com.skyblock21;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.config.Skyblock21Screen;
import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.events.SkyblockEvents;
import com.skyblock21.features.*;
import com.skyblock21.features.commandaliases.CommandAliasesScreen;
import com.skyblock21.features.foraging.HOTFOverlay;
import com.skyblock21.features.foraging.TreeProgress;
import com.skyblock21.features.foraging.treewaypoints.TreeWaypoints;
import com.skyblock21.features.itemcustomization.ItemCustomizationScreen;
import com.skyblock21.features.items.StarredDropPrevention;
import com.skyblock21.features.keyshortcuts.KeyShortcuts;
import com.skyblock21.features.keyshortcuts.KeyShortcutsScreen;
import com.skyblock21.features.kuudra.Kuudra;
import com.skyblock21.features.waypoints.WaypointRenderer;
import com.skyblock21.gui.Theme;
import com.skyblock21.gui.ThemeManager;
import com.skyblock21.hud.EditGuiScreen;
import com.skyblock21.hud.HudManager;
import com.skyblock21.hud.elements.*;
import com.skyblock21.tracking.BaseTracker;
import com.skyblock21.tracking.TrackerManager;
import com.skyblock21.util.*;
import com.skyblock21.util.dev.AutoUpdater;
import com.skyblock21.util.tab.TabUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Skyblock21 implements ClientModInitializer {
    public static final String MOD_ID = "skyblock21";
    public static final String MOD_VERSION = "1.4.0";
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final MinecraftClient client = MinecraftClient.getInstance();

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registry) -> {
            dispatcher.register(createSkyblockCommand("skyblock21"));
            dispatcher.register(createSkyblockCommand("sb21"));
        });
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> createSkyblockCommand(String commandName) {
        return literal(commandName)
                .executes((ctx) -> {
                    client
                                   .send(() -> client
                                                              .setScreen(new Skyblock21Screen()));
                    return 1;
                })
                .then(literal("config").executes((ctx) -> {
                    client
                                   .send(() -> client
                                                              .setScreen(Skyblock21ConfigManager.createGUI(null)));
                    return 1;
                }))
                .then(literal("gui").executes((ctx) -> {
                    client
                                   .send(() -> client
                                                              .setScreen(new EditGuiScreen(client.currentScreen)));
                    return 1;
                }))
                .then(literal("keys").executes((ctx) -> {
                    client
                                   .send(() -> client
                                                              .setScreen(new KeyShortcutsScreen(client.currentScreen)));
                    return 1;
                }))
                .then(literal("aliases").executes((ctx) -> {
                    client
                                   .send(() -> client
                                                              .setScreen(new CommandAliasesScreen(client.currentScreen)));
                    return 1;
                })).then(literal("update").executes((ctx) -> {
                    AutoUpdater.checkForUpdateAsyncAndRestart();

                    return 1;
                })).then(literal("customize").executes((ctx) -> {
                    if (client.player == null) return 1;

                    ItemStack heldItem = client.player.getMainHandStack();

                    if (heldItem.isEmpty()) {
                        TextUtils.addMessage("§cYou must hold an item to customize it!", true, false);
                        return 0;
                    }

                    String itemUuid = ItemUtils.getItemUUID(heldItem);
                    if (itemUuid.isEmpty()) {
                        TextUtils.addMessage("§cThis item cannot be customized!", true, false);
                        return 0;
                    }

                    client
                                   .send(() -> client
                                           .setScreen(new ItemCustomizationScreen(heldItem)));

                    return 1;
                }));
    }

    public void tick(MinecraftClient client) {
        Utils.update();
    }

    @Override
    public void onInitializeClient() {
        AutoUpdater.initialize();

        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        TickScheduler.getInstance();

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
        RunicMobsHighlight.init();

        // Mining
        Scathas.init();

        // Galatea
        TreeProgress.init();
        HOTFOverlay.init();
        TreeWaypoints.init();

        // Hunting
        SpinHelper.init();

        // Items
        StarredDropPrevention.init();

        // Kuudra
        Kuudra.init();

        HudManager.register(new TestHudElement(100, 50));
        HudManager.register(new TreeProgressHudElement(100, 190));
        HudManager.register(new GalateaTrackerElement(10, 10));
        HudManager.register(new BonusGiftsTrackerElement(10, 40));
        HudManager.register(new ScathaTrackerElement(10, 60));
        HudManager.register(new HuntingTrackerElement(30, 20));
        HudManager.init();
        registerCommands();

        ThemeManager.setTheme(PersistentData.get().theme);

        WorldRenderEvents.LAST.register(context -> {
            WaypointRenderer.renderWaypoints(
                    context,
                    context.camera(),
                    context.tickCounter().getDynamicDeltaTicks()
            );
        });

        SkyblockEvents.JOIN.register(() -> {
            TrackerManager.getAllTrackers().forEach(BaseTracker::pauseTracker);
            TickSchedulerHelper.runAfterSeconds(() -> {
                if (client.player == null || client.world == null) return;
                Text message = Text.literal(TextUtils.PREFIX + " §fThis mod is actively looking to ")
                                   .append(Text.literal("\n§fadd new features!  §9§l[SUGGEST NEW FEATURES]")
                                               .styled(style -> style.withClickEvent(new ClickEvent.OpenUrl(URI.create("https://discord.gg/NMNSwQH6dr")))
                                                                     .withHoverEvent(new HoverEvent.ShowText(Text.literal("§eClick to join Discord and suggest features")))));

                client.player.sendMessage(Text.literal("§7§m                                                              "), false);
                client.player.sendMessage(message, false);
                client.player.sendMessage(Text.literal("§7§m                                                              "), false);
            }, 2);
        });

        SkyblockEvents.LOCATION_CHANGE.register((location -> {
            PersistentData.save();
            TrackerManager.getAllTrackers().forEach(BaseTracker::pauseTracker);
        }));

        SkyblockEvents.LEAVE.register(TrackerManager::saveAllTrackers);

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            TrackerManager.shutdown();
            Skyblock21ConfigManager.save();
            PersistentData.save();
        });
    }
}