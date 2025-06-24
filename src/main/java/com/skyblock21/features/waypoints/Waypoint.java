package com.skyblock21.features.waypoints;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class Waypoint {

    private String name;
    private BlockPos position;
    private int color;
    private UUID uuid;

    private boolean beaconBeam;

    private boolean circleOnFloor;

    private boolean visible;

    public Waypoint(UUID uuid, String name, BlockPos position, boolean beaconBeam, int color) {
        this.uuid = uuid;
        this.name = name;
        this.position = position;
        this.beaconBeam = beaconBeam;
        this.color = color;
        this.visible = true;
    }

    public Waypoint(String name, BlockPos position, int color) {
        this(name, position, false, color);
    }

    public Waypoint(String name, BlockPos position, boolean beaconBeam, int color) {
        this(name, position, beaconBeam, color, UUID.randomUUID());
    }

    public Waypoint(String name, BlockPos position, boolean beaconBeam, int color, UUID uuid) {
        this.uuid = uuid;
        this.name = name;
        this.position = position;
        this.color = color;
        this.beaconBeam = beaconBeam;
        this.visible = true;
    }

    public String getName() {
        return name;
    }

    public BlockPos getPosition() {
        return position;
    }

    public int getColor() {
        return color;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isBeaconBeam() {
        return beaconBeam;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(BlockPos position) {
        this.position = position;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setBeaconBeam(boolean beaconBeam) {
        this.beaconBeam = beaconBeam;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isCircleOnFloor() {
        return circleOnFloor;
    }

    public void setCircleOnFloor(boolean circleOnFloor) {
        this.circleOnFloor = circleOnFloor;
    }

    public double getDistance(Vec3d playerPos) {
        return Math.sqrt(position.getSquaredDistance(playerPos));
    }
}
