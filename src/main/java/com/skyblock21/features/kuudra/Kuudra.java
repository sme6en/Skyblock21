package com.skyblock21.features.kuudra;

import com.skyblock21.events.ChatEvents;
import com.skyblock21.events.EntityEvents;
import com.skyblock21.events.SkyblockEvents;
import com.skyblock21.features.waypoints.Waypoint;
import com.skyblock21.features.waypoints.WaypointManager;
import com.skyblock21.features.waypoints.WaypointRenderer;
import com.skyblock21.util.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Kuudra {

    public static int currentPhase = 0;

    public static final Map<Vec3d, UUID> supplyLocations = new HashMap<>();

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(Kuudra::onTick);
        ChatEvents.RECEIVE_TEXT.register(Kuudra::onChat);

        SkyblockEvents.LOCATION_CHANGE.register((location) -> {
            currentPhase = 0;
            supplyLocations.clear();
            SupplyWaypoints.supplyEntities.clear();
            WaypointManager.removeAllWaypoints();
            SupplyWaypoints.supplies = new boolean[]{true, true, true, true, true, true};
        });

        SupplyWaypoints.init();

        supplyLocations.put(new Vec3d(-98, 73, -112), UUID.randomUUID());
        supplyLocations.put(new Vec3d(-98, 73, -99), UUID.randomUUID());
        supplyLocations.put(new Vec3d(-110, 73, -106), UUID.randomUUID());
        supplyLocations.put(new Vec3d(-106, 73, -112), UUID.randomUUID());
        supplyLocations.put(new Vec3d(-94, 73, -106), UUID.randomUUID());
        supplyLocations.put(new Vec3d(-106, 73, -99), UUID.randomUUID());
    }

    private static void onChat(Text text) {
        if(!Utils.isInKuudra()) return;

        String message = text.getString();

        switch (message) {
            case "§e[NPC] §cElle§f: Okay adventurers, I will go and fish up Kuudra!" -> currentPhase = 1;
            case "§e[NPC] §cElle§f: OMG! Great work collecting my supplies!" -> currentPhase = 2;
            case "§e[NPC] §cElle§f: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!" -> currentPhase = 3;
            case "§e[NPC] §cElle§f: The Ballista is ready! Let's go!" -> currentPhase = 4;
        }

        if (currentPhase == 1) {
            supplyLocations.forEach((pos, uuid) -> {
                Waypoint w = WaypointManager.addWaypoint(uuid, "", BlockPos.ofFloored(pos), Color.GREEN.getRGB());
                w.setCircleOnFloor(true);
            });
        }
    }

    private static void onTick(MinecraftClient client) {
        if (!Utils.isInKuudra()) return;
        if (client.world == null || client.player == null) return;

        for (Entity entity : client.world.getEntities()) {
            if (!(entity instanceof ArmorStandEntity armorStand)) continue;

            if (currentPhase != 1) continue;
            if (armorStand.getCustomName() == null) continue;
            if (!armorStand.getCustomName().getString().contains("✓ SUPPLIES RECEIVED ✓")) continue;

            Vec3d pos = armorStand.getPos();
            UUID uuid = supplyLocations.get(new Vec3d((int)pos.x, 73, (int)pos.z));
            if (uuid == null) continue;

            WaypointManager.removeWaypointsIfMatch(uuid);
        }

    }
}
