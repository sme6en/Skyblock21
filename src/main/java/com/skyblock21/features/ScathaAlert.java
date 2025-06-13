package com.skyblock21.features;

import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.util.Utils;
import kotlin.collections.ArrayDeque;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundEngine;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;

import java.util.ArrayList;
import java.util.List;

public class ScathaAlert {

    public long timeSincePreviousSpawn;
    private List<String> uuids = new ArrayList<>();

    public static void init() {
        ClientEntityEvents.ENTITY_LOAD.register((entity, clientWorld) -> {
            if (!Utils.isInCrystalHollows()) return;
            if (!(entity instanceof ArmorStandEntity)) return;
            if (!Skyblock21ConfigManager.get().mining.scathaAlerts) return;

            String name = entity.hasCustomName() ? entity.getCustomName().getString() : "";
            System.out.println(name);
            if (entity == null) return;

            if (name.startsWith("[Lv5] Worm")) {
                alert(false);
            } else if (name.startsWith("[Lv10] Scatha")) {
                alert(false);
            }

        });
    }

    public static void onTick(MinecraftClient client) {
        if (!Utils.isInCrystalHollows()) return;
        if (!Skyblock21ConfigManager.get().mining.scathaAlerts) return;

        List<ArmorStandEntity> entityList = client.world.getEntitiesByClass(ArmorStandEntity.class, client.player.getBoundingBox().expand(80d), e -> e.hasCustomName() && (e.getCustomName().getString().startsWith("[Lv5] Worm") || e.getCustomName().getString().startsWith("[Lv10] Scatha")));

        if (entityList.isEmpty()) return;
    }

    public static void alert(boolean isScatha) {
        MinecraftClient client = MinecraftClient.getInstance();
        try {
            client.getNetworkHandler()
                  .onTitle(new TitleS2CPacket(Text.of(isScatha ? "§cScatha has spawned!" : "§8Worm has spawned!"))); // title, subtitle, fadeIn, stay, fadeOut ticks;
            if(isScatha) client.getNetworkHandler().onSubtitle(new SubtitleS2CPacket(Text.of("Pray to RNGesus")));
            client.getNetworkHandler().onTitleFade(new TitleFadeS2CPacket(2, 30, 2)); // fadeIn, stay, fadeOut ticks
            MinecraftClient.getInstance()
                           .getSoundManager()
                           .play(PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F), 1);
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
//        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F), 5);
//        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F), 10);
    }
}
