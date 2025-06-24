package com.skyblock21.features.kuudra;

import com.skyblock21.features.waypoints.Waypoint;
import com.skyblock21.features.waypoints.WaypointManager;
import com.skyblock21.features.waypoints.WaypointRenderer;
import com.skyblock21.util.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SupplyWaypoints {

    public static List<ArmorStandEntity> supplyEntities = new ArrayList<>();
    // array of 6 true
    public static boolean[] supplies = {true, true, true, true, true, true};

    private static final Pattern progressRegex = Pattern.compile("PROGRESS: (\\d+)%");
    private static final Pattern buildRegex = Pattern.compile("Building Progress (\\d+)% \\((\\d+) Players Helping\\)");

    public static void init() {
        WorldRenderEvents.LAST.register((context) -> {
            WaypointRenderer.renderWaypoints(
                    context,
                    context.camera(),
                    context.tickCounter().getDynamicDeltaTicks()
            );
        });
        ClientTickEvents.END_CLIENT_TICK.register(SupplyWaypoints::onTick);
    }

    public static void onTick(MinecraftClient client) {
        if (!Utils.isInKuudra()) return;
        if (client.player == null || client.world == null) return;

        List<ArmorStandEntity> supplyEntitiesCache = new ArrayList<>(SupplyWaypoints.supplyEntities);
        for (ArmorStandEntity entity : supplyEntitiesCache) {
            if (entity.isRemoved()) {
                supplyEntities.remove(entity);
                WaypointManager.removeWaypointsIfMatch(entity.getUuid());
            } else {
                BlockPos pos = entity.getBlockPos();
                WaypointManager.updateWaypointPosition(entity.getUuid(), pos);
            }
        }

        for (Entity entity : client.world.getEntities()) {
            if (!(entity instanceof ArmorStandEntity armorstand)) continue;
            if (armorstand.getCustomName() == null) continue;

            if (armorstand.getCustomName().getString().contains("SUPPLIES")) {
                supplyEntities.add(armorstand);
                BlockPos pos = armorstand.getBlockPos();
                Waypoint w = WaypointManager.addWaypoint(armorstand.getUuid(), "Supply", pos);
                w.setBeaconBeam(true);
            }
        }
    }
}
