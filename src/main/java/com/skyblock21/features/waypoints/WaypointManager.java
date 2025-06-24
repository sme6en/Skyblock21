package com.skyblock21.features.waypoints;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WaypointManager {

    private static final Map<UUID, Waypoint> waypoints = new ConcurrentHashMap<>();

    public static final int DEFAULT_COLOR = 0x6B96EF;

    public static Waypoint addWaypoint(UUID uuid, String name, BlockPos pos) {
        Waypoint waypoint = new Waypoint(uuid, name, pos, false, DEFAULT_COLOR);

        waypoints.put(waypoint.getUuid(), waypoint);

        return waypoint;
    }

    public static Waypoint addWaypoint(UUID uuid, String name, BlockPos pos, int color) {
        Waypoint waypoint = new Waypoint(uuid, name, pos, false, color);

        waypoints.put(waypoint.getUuid(), waypoint);

        return waypoint;
    }

    public static Waypoint addWaypoint(BlockPos pos, boolean beaconBeam) {
        Waypoint waypoint = new Waypoint("", pos, beaconBeam, DEFAULT_COLOR);

        waypoints.put(waypoint.getUuid(), waypoint);

        return waypoint;
    }

    public static Waypoint addWaypoint(String name, BlockPos pos, boolean beaconBeam) {
        Waypoint waypoint = new Waypoint(name, pos, beaconBeam, DEFAULT_COLOR);

        waypoints.put(waypoint.getUuid(), waypoint);

        return waypoint;
    }

    public static List<Waypoint> getVisibleWaypoints() {
        List<Waypoint> visibleWaypoints = new ArrayList<>();
        for (Waypoint waypoint : waypoints.values()) {
            if (waypoint.isVisible()) {
                visibleWaypoints.add(waypoint);
            }
        }
        return visibleWaypoints;
    }

    public static Waypoint getWaypoint(UUID uuid) {
        return waypoints.get(uuid);
    }

    public static void updateWaypointPosition(UUID uuid, BlockPos newPos) {
        Waypoint waypoint = getWaypoint(uuid);
        if (waypoint != null) {
            waypoint.setPosition(newPos);
        }
    }

    public static void removeWaypointsIfMatch(String name) {
        waypoints.values().removeIf(waypoint -> waypoint.getName().contains(name));
    }

    public static void removeWaypointsIfMatch(UUID uuid) {
        waypoints.values().removeIf(waypoint -> waypoint.getUuid().equals(uuid));
    }

    public static void removeAllWaypoints() {
        waypoints.clear();
    }
}
