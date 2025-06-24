package com.skyblock21.features;

import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.util.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.HashMap;

public class HideAroundNPC {

    private static final int HIDE_RADIUS_SQUARED = (int) Math.round(3 * 3);

    private static final Map<String, Vec3d> npcLocations = new HashMap<>();

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(HideAroundNPC::onClientTick);
    }

    private static int ticks = 0;
    private static void onClientTick(MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        if (!Utils.isOnSkyblock()) return;
        if (!Skyblock21ConfigManager.get().general.hidePlayersAroundNpcs) return;

        if (ticks++ < 20) {
            return;
        } else {
            ticks = 0;
        }

        // Iterate through all entities and store NPC locations
        for (Entity entity : client.world.getEntities()) {
            if (!(entity instanceof OtherClientPlayerEntity otherPlayer)) {
                continue;
            }

            if (entity.age < 5) continue;
            float health = otherPlayer.getHealth();
            if (npcLocations.containsKey(entity.getUuid().toString())) {
                if (health != 20.0F) {
                    npcLocations.remove(entity.getUuid().toString());
                }
            } else if (isNPC(entity)) {
                npcLocations.put(entity.getUuid().toString(), entity.getPos());
            }
        }
    }

    public static boolean isNearNPC(Entity entityToCheck) {
        for (Vec3d npcLocation : npcLocations.values()) {
            if (entityToCheck.squaredDistanceTo(npcLocation) <= HIDE_RADIUS_SQUARED) {
                return true;
            }
        }

        return false;
    }

    public static boolean isNPC(Entity entity) {
        if (!(entity instanceof OtherClientPlayerEntity)) {
            return false;
        }
        LivingEntity livingEntity = (LivingEntity) entity;
        return entity.getUuid().version() == 2 && livingEntity.getHealth() == 20.0F && !livingEntity.isSleeping();
    }
}
