package com.skyblock21.features;

import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.events.ChatEvents;
import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.util.TextUtils;
import com.skyblock21.util.Utils;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

public class Scathas {

    private static ArmorStandEntity currentEntity = null;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(Scathas::onTick);
        ChatEvents.RECEIVE_TEXT.register(Scathas::onChat);
    }

    private static void onChat(Text text) {
        if (!Utils.isInCrystalHollows()) return;

        String message = text.getString();

        if (message.contains("§6§lPET DROP! ") && message.contains("Scatha")) {
            PersistentData.get().scathasData.lastScathaPetDropTime = System.currentTimeMillis();
            PersistentData.get().scathasData.sinceLastScathaPetDropSpawns = 0;
        }
    }

    public static void onTick(MinecraftClient client) {
        if (!Utils.isInCrystalHollows()) return;
        if (client.world == null || client.player == null) return;

        if (currentEntity != null) {
            if (client.world.getEntity(currentEntity.getUuid()) == null) {
                handleDespawn(currentEntity);
                currentEntity = null;
                return;
            }
        }

        ArmorStandEntity worm = client.world.getEntitiesByClass(ArmorStandEntity.class, client.player.getBoundingBox()
                                                                                                     .expand(30d, 6d, 30d), e -> e.hasCustomName() && (e.getCustomName()
                                                                                                                                                        .getString()
                                                                                                                                                        .startsWith("[Lv5] Worm") || e.getCustomName()
                                                                                                                                                                                      .getString()
                                                                                                                                                                                      .startsWith("[Lv10] Scatha")))
                                            .stream()
                                            .findFirst()
                                            .orElse(null);
        if (worm == null) return;
        if (currentEntity != null && worm.getUuid().equals(currentEntity.getUuid())) return;

        currentEntity = worm;
        PersistentData.get().scathasData.lastSpawnTime = System.currentTimeMillis();

        boolean isScatha = worm.getCustomName().getString().startsWith("[Lv10] Scatha");
        handleSpawn(isScatha);
    }

    public static void handleSpawn(boolean isScatha) {
        ScathasData data = PersistentData.get().scathasData;
        long currentTime = System.currentTimeMillis();
        data.lastSpawnTime = currentTime;
        data.sinceLastScathaPetDropSpawns++;
        if (isScatha) {
            data.scathasSpawned++;
        } else {
            data.wormsSpawned++;
        }

        PersistentData.get().scathasData = data;
        alert(isScatha);
    }

    public static void handleDespawn(ArmorStandEntity entity) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        Box killBox = player.getBoundingBox().expand(20d, 255d, 20d);
        ScathasData data = PersistentData.get().scathasData;


        if (System.currentTimeMillis() - data.lastSpawnTime > 28000) {
            TextUtils.addMessage("§cWorm despawned!", true, false);
        } else if (entity.getBoundingBox().intersects(killBox)) {
            if (entity.getCustomName().getString().startsWith("[Lv10] Scatha")) {
                TextUtils.addMessage("§aScatha killed! §7(" + data.scathasSpawned + " this session)", true, false);
            } else {
                TextUtils.addMessage("§aWorm killed! §7(" + data.wormsSpawned + " this session)", true, false);
            }
            PersistentData.get().scathasData.lastScathaKillTime = System.currentTimeMillis();
        } else {
            TextUtils.addMessage("§cWorm despawned out of range!", true, false);
        }
    }

    public static void alert(boolean isScatha) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!Skyblock21ConfigManager.get().mining.scathaTracker) return;

        try {
            client.getNetworkHandler()
                  .onTitle(new TitleS2CPacket(Text.of(isScatha ? "§cScatha has spawned!" : "§cWorm has spawned!"))); // title, subtitle, fadeIn, stay, fadeOut ticks;
            if (isScatha) client.getNetworkHandler().onSubtitle(new SubtitleS2CPacket(Text.of("Pray to RNGesus")));
            client.getNetworkHandler().onTitleFade(new TitleFadeS2CPacket(2, 30, 2)); // fadeIn, stay, fadeOut ticks
            client
                    .getSoundManager()
                    .play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1.0f, 1.0f), 1);
            client
                    .getSoundManager()
                    .play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1.0f, 2.0f), 10);
            client
                    .getSoundManager()
                    .play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1.0f, 2.0f), 20);
        } catch (Exception e) {
        }
    }

    public static String getHudText() {
        ScathasData data = PersistentData.get().scathasData;

        return String.format("""
                        §bWorms: §f%d
                        §bScathas: §f%d §7(%.0f%%)
                        §7Spawns since pet drop: §f%d
                        §7Last Scatha spawn: §f%s ago
                        %s
                        """,
                data.wormsSpawned,
                data.scathasSpawned,
                data.scathasSpawned > 0 ? (data.scathasSpawned * 100.0 / (data.wormsSpawned + data.scathasSpawned)) : 0.0,
                data.sinceLastScathaPetDropSpawns,
                data.lastSpawnTime == -1 || System.currentTimeMillis() - data.lastSpawnTime >= 24 * 60 * 60 * 1000 ? "N/A" : Utils.formatTime(System.currentTimeMillis() - data.lastSpawnTime),
                System.currentTimeMillis() - data.lastScathaKillTime < 30000 ? "§cCannot spawn Scatha yet! " + Utils.formatTime(
                        30000 - (System.currentTimeMillis() - data.lastScathaKillTime)
                ) : ""
        );
    }

    public static String getDummyHudText() {
        return String.format("""
                        §bWorms: §f%d
                        §bScathas: §f%d §7(%.0f%%)
                        §7Spawns since pet drop: §f%d
                        §7Last Scatha spawn: §f%s ago
                        %s
                        """,
                123,
                24,
                19.0,
                4,
                "12s",
                ""
        );
    }

    public static void resetSession() {
        ScathasData data = PersistentData.get().scathasData;
        data.lastSpawnTime = -1;
        data.lastScathaKillTime = -1;
        data.lastScathaPetDropTime = -1;
        data.sinceLastScathaPetDropSpawns = 0;
        currentEntity = null;
        data.wormsSpawned = 0;
        data.scathasSpawned = 0;

        PersistentData.get().scathasData = data;
    }

    public static class ScathasData {
        @SerialEntry
        public long lastSpawnTime = -1;
        @SerialEntry
        public long lastScathaKillTime = -1;
        @SerialEntry
        public long lastScathaPetDropTime = -1;
        @SerialEntry
        public int sinceLastScathaPetDropSpawns = 0;
        @SerialEntry
        public int wormsSpawned = 0;
        @SerialEntry
        public int scathasSpawned = 0;
    }
}
