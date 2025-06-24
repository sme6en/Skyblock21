package com.skyblock21.features.kuudra;

import com.skyblock21.features.waypoints.Waypoint;
import com.skyblock21.features.waypoints.WaypointManager;
import com.skyblock21.features.waypoints.WaypointRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.GiantEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class SupplyWaypoints {

    private static final HashMap<Vec3d, String> supplyLocations = new HashMap<>();
    public static List<GiantEntity> supplyEntities = new ArrayList<>();
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
    }

    public static void onTick(MinecraftClient client) {
        for (GiantEntity entity : supplyEntities) {
            if (entity.isRemoved()) {
                supplyEntities.remove(entity);
                WaypointManager.removeWaypointsIfMatch(entity.getUuid());
            } else {
                BlockPos pos = entity.getBlockPos();
                WaypointManager.updateWaypointPosition(entity.getUuid(), pos);
            }
        }
    }

    public static void onEntitySpawn(Entity entity, int entityId) {
        if (!(entity instanceof GiantEntity giant)) return;

        System.out.println("SupplyWaypoints - Held item: " + giant.getMainHandStack().getName().getString());
        if (giant.getMainHandStack().getName().getString().contains("head")) {
            supplyEntities.add(giant);
            BlockPos pos = giant.getBlockPos();
            Waypoint w = WaypointManager.addWaypoint(giant.getUuid(), "Supply", pos);
        }
    }

}
