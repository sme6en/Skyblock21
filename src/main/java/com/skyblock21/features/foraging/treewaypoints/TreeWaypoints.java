package com.skyblock21.features.foraging.treewaypoints;

import com.skyblock21.Skyblock21;
import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.events.*;
import com.skyblock21.features.waypoints.Waypoint;
import com.skyblock21.features.waypoints.WaypointManager;
import com.skyblock21.features.waypoints.WaypointRenderer;
import com.skyblock21.util.Location;
import com.skyblock21.util.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.awt.*;
import java.io.BufferedReader;
import java.util.*;
import java.util.List;
import java.util.Queue;

public class TreeWaypoints {

    public static final Map<UUID, Tree> trees = new HashMap<>();
    private static final int SCAN_RADIUS = 1;
    private static int ticks = 0;
    private static Tree nearestTree = null;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(TreeWaypoints::tick);
        SkyblockEvents.LOCATION_CHANGE.register(TreeWaypoints::onLocationChange);
        ParticleEvents.SPAWN.register(TreeWaypoints::onParticle);
        ChunkEvents.CHUNK_REMOVED.register(TreeWaypoints::onChunkRemoved);
        ChunkEvents.CHUNK_SPAWNED.register(TreeWaypoints::onChunkSpawned);
        BlockEvents.BLOCK_ADDED.register(TreeWaypoints::onBlockAdded);
    }

    private static void onBlockAdded(World world, BlockPos blockPos, BlockState blockState) {
        if (!Utils.isOnSkyblock()) return;
        if (!Utils.isInGalatea()) return;
        if (!Skyblock21ConfigManager.get().foraging.treeWaypoints) return;
        Block block = blockState.getBlock();
        if (block != Blocks.STRIPPED_SPRUCE_WOOD && block != Blocks.MANGROVE_WOOD) return;

        for (Tree tree : trees.values()) {
            if (tree.getState() == TreeState.REGENERATING) continue;
            if (tree.getLogBlock() != block) continue;

            if (tree.basePos.equals(blockPos) || tree.knownLogPositions.contains(blockPos)) {
                tree.setState(TreeState.REGENERATING);
                tree.stateStartTime -= (long) (tree.getRegeneratingDuration() * 0.1);
                return;
            }
        }
    }

    private static void onChunkSpawned(ClientWorld clientWorld, WorldChunk worldChunk) {
        if (!Utils.isOnSkyblock()) return;
        if (!Utils.isInGalatea()) return;
        if (!Skyblock21ConfigManager.get().foraging.treeWaypoints) return;

        ChunkPos chunkPos = worldChunk.getPos();

        for (Tree tree : trees.values()) {
            int chunkX = tree.basePos.getX() >> 4;
            int chunkZ = tree.basePos.getZ() >> 4;

            if (chunkX == chunkPos.x && chunkZ == chunkPos.z) {
                updateTreeStateMachine(tree);
            }
        }
    }

    private static void onChunkRemoved(ClientWorld clientWorld, WorldChunk worldChunk) {
        if (!Utils.isOnSkyblock()) return;
        if (!Utils.isInGalatea()) return;
        if (!Skyblock21ConfigManager.get().foraging.treeWaypoints) return;

        ChunkPos chunkPos = worldChunk.getPos();

        for (Tree tree : trees.values()) {
            int chunkX = tree.basePos.getX() >> 4;
            int chunkZ = tree.basePos.getZ() >> 4;

            if (chunkX == chunkPos.x && chunkZ == chunkPos.z) {
                tree.setState(TreeState.NONE);
            }
        }
    }

    private static void onParticle(ParticleS2CPacket particleS2CPacket) {
        if (!Utils.isOnSkyblock()) return;
        if (!Utils.isInGalatea()) return;
        if (!Skyblock21ConfigManager.get().foraging.treeWaypoints) return;

        ParticleType<?> particleType = particleS2CPacket.getParameters().getType();
        if (particleType != ParticleTypes.GUST) return;

        double particleX = particleS2CPacket.getX();
        double particleY = particleS2CPacket.getY();
        double particleZ = particleS2CPacket.getZ();

        Tree closestTree = null;
        double closestDistance = Double.MAX_VALUE;
        for (Tree tree : trees.values()) {
            if (!isTreeAllowed(tree)) continue;

            double distance = new Vec3d(particleX, particleY, particleZ).squaredDistanceTo(tree.basePos.getX(), tree.basePos.getY(), tree.basePos.getZ());
            if (distance < closestDistance) {
                closestDistance = distance;
                closestTree = tree;
            }
        }

        if (closestTree != null && closestDistance <= 20) {
            closestTree.setState(TreeState.NOT_PRESENT);
        }

    }

    private static void onLocationChange(Location location) {

        if (location == Location.GALATEA) {
            performInitialWorldScan();
        } else {
            trees.clear();
            WaypointManager.removeAllWaypoints();
        }
    }

    public static void loadTrees() {
        try (BufferedReader r = MinecraftClient.getInstance().getResourceManager().openAsReader(Identifier.of("skyblock21", "tree_locations.json"))) {

            Tree[] loadedTrees = Skyblock21.GSON.fromJson(r, Tree[].class);
            for (Tree tree : loadedTrees) {
                tree.basePos = new BlockPos(tree.x, tree.y, tree.z);
                tree.currentState = TreeState.NONE;
                tree.knownLogPositions = new HashSet<>();
                tree.stateStartTime = System.currentTimeMillis();
                tree.waypointId = UUID.randomUUID();

                trees.put(tree.waypointId, tree);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isTreeAllowed(Tree tree) {
        boolean onlySmall = Skyblock21ConfigManager.get().foraging.onlyShowSmallTrees;
        boolean mangrove = Skyblock21ConfigManager.get().foraging.showMangroveTreeWaypoints;
        boolean fig = Skyblock21ConfigManager.get().foraging.showFigTreeWaypoints;

        if (onlySmall && tree.isBigTree) {
            return false;
        }

        if (tree.isMangrove) {
            return mangrove;
        } else {
            return fig;
        }
    }

    public static void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) return;
        if (++ticks % 2 != 0) return;

        if (!Utils.isOnSkyblock()) return;
        if (!Utils.isInGalatea()) return;
        if (!Skyblock21ConfigManager.get().foraging.treeWaypoints) return;

        for (Tree tree : trees.values()) {
            updateTreeStateMachine(tree);
        }
        BlockPos playerPos = client.player.getBlockPos();
        nearestTree = findSecondNearestSmallTree(playerPos);
        updateAllWaypoints();

        if (ticks == 20) ticks = 0;
    }

    private static void updateTreeStateMachine(Tree tree) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        Set<BlockPos> logPositions = scanAreaForLogs(client.world, tree);
        Set<BlockPos> connectedLogs = logPositions.isEmpty() ? new HashSet<>() : scanNeighborsForTree(client.world, logPositions, tree);

        boolean treePhysicallyPresent = !connectedLogs.isEmpty();
        long timeInState = tree.getTimeInCurrentState();

        if ((tree.currentState == TreeState.PRESENT || tree.currentState == TreeState.NONE) && ticks % 20 != 0) return;

        switch (tree.currentState) {
            case NONE:
                if (treePhysicallyPresent) {
                    tree.setState(TreeState.PRESENT);
                    tree.knownLogPositions = new HashSet<>(connectedLogs);
                }
            case NOT_PRESENT:
                if (timeInState >= tree.NOT_PRESENT_DURATION) {
                    tree.setState(TreeState.REGENERATING);
                }
                break;

            case REGENERATING:
                if (timeInState >= tree.getRegeneratingDuration()) {
                    tree.setState(TreeState.PRESENT);
                }
                break;

            case PRESENT:
                if (!treePhysicallyPresent) {
                    tree.setState(TreeState.NOT_PRESENT);
                } else {
                    tree.knownLogPositions = new HashSet<>(connectedLogs);
                }
                break;
        }
    }

    private static Set<BlockPos> scanAreaForLogs(World world, Tree tree) {
        Set<BlockPos> logPositions = new HashSet<>();

        int baseX = tree.basePos.getX();
        int baseZ = tree.basePos.getZ();

        for (int x = baseX - SCAN_RADIUS; x <= baseX + SCAN_RADIUS; x++) {
            for (int z = baseZ - SCAN_RADIUS; z <= baseZ + SCAN_RADIUS; z++) {
                for (int y = tree.y; y <= tree.maxY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    Block block = world.getBlockState(pos).getBlock();

                    if (block == tree.getLogBlock()) {
                        logPositions.add(pos);
                    }
                }
            }
        }

        return logPositions;
    }

    private static Set<BlockPos> scanNeighborsForTree(World world, Set<BlockPos> initialLogs, Tree tree) {
        Set<BlockPos> connectedLogs = new HashSet<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        if (!initialLogs.isEmpty()) {
            BlockPos start = initialLogs.iterator().next();
            queue.add(start);
            visited.add(start);
        }

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            connectedLogs.add(current);

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;

                        BlockPos neighbor = current.add(dx, dy, dz);

                        if (!visited.contains(neighbor)) {
                            Block block = world.getBlockState(neighbor).getBlock();

                            if (block == tree.getLogBlock()) {
                                visited.add(neighbor);
                                queue.add(neighbor);
                            }
                        }
                    }
                }
            }
        }

        return connectedLogs.size() >= 2 ? connectedLogs : new HashSet<>();
    }

    private static void updateAllWaypoints() {
        for (Tree tree : trees.values()) {
            updateWaypointForTree(tree);
        }
    }

    private static void updateWaypointForTree(Tree tree) {
        Waypoint waypoint = WaypointManager.getWaypoint(tree.waypointId);

        String name = getWaypointName(tree);
        int color = getWaypointColor(tree);
        boolean visible = shouldWaypointBeVisible(tree);
        boolean onlyNearest = Skyblock21ConfigManager.get().foraging.onlyNearestTree;

        if (onlyNearest && !tree.equals(nearestTree)) {
            visible = false;
        }

        if (waypoint == null && visible) {
            waypoint = WaypointManager.addWaypoint(tree.waypointId, name, tree.getCenterPos(), color);
            waypoint.setBeaconBeam(!Skyblock21ConfigManager.get().foraging.noBeaconBeams);
        } else if (waypoint != null) {
            waypoint.setVisible(visible);
            waypoint.setBeaconBeam(!Skyblock21ConfigManager.get().foraging.noBeaconBeams);
            if (visible) {
                waypoint.setPosition(tree.basePos);
                waypoint.setName(name);
                waypoint.setColor(color);
                waypoint.setHideWhenClose(tree.currentState == TreeState.PRESENT);
            }
        }
    }

    private static String getWaypointName(Tree tree) {
        String treeName = tree.getTreeTypeName();

        if (!tree.isBig()) {
            BlockPos playerPos = MinecraftClient.getInstance().player.getBlockPos();
            if (playerPos != null) {

                if (nearestTree != null && nearestTree.waypointId.equals(tree.waypointId)) {
                    treeName += " §a(nearest)";
                }
            }
        }

        switch (tree.getState()) {
            case NOT_PRESENT:
                return treeName + " Broken";

            case REGENERATING:
                long totalDuration = tree.isBig() ? 24000 : 12000;
                long regenSecondsLeft = (totalDuration - tree.getTimeInState()) / 1000;
                return treeName + (Skyblock21ConfigManager.get().foraging.timeBeforeReady >= regenSecondsLeft ? " §7(" + Math.max(0, regenSecondsLeft) + "s)" : "");

            case PRESENT:
                return treeName;

            default:
                return "Unknown Tree";
        }

    }

    private static int getWaypointColor(Tree tree) {
        switch (tree.getState()) {
            case REGENERATING:
                return 0xFFFF00;

            case PRESENT:
                if (tree.isMangrove()) {
                    return tree.isBig() ? 0x8B4513 : 0xD2691E;
                } else {
                    return tree.isBig() ? 0x006400 : 0x32CD32;
                }

            default:
                return 0xFFFFFF;
        }
    }

    private static boolean shouldWaypointBeVisible(Tree tree) {
        BlockPos playerPos = MinecraftClient.getInstance().player.getBlockPos();
        if (playerPos == null) return false;

        int distance = (int) playerPos.getSquaredDistance(tree.basePos.getX(), tree.basePos.getY(), tree.basePos.getZ());
        int maxDistance = Skyblock21ConfigManager.get().foraging.maxDistance;

        return tree.currentState != TreeState.NOT_PRESENT && tree.currentState != TreeState.NONE && isTreeAllowed(tree) && (maxDistance == 0 || distance <= maxDistance * maxDistance);
    }

    public static void performInitialWorldScan() {
        if (trees.isEmpty()) {
            loadTrees();
            return;
        }

        for (Tree tree : trees.values()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null) continue;

            Set<BlockPos> logPositions = scanAreaForLogs(client.world, tree);
            Set<BlockPos> connectedLogs = logPositions.isEmpty() ? new HashSet<>() : scanNeighborsForTree(client.world, logPositions, tree);

            if (connectedLogs.isEmpty()) {
                tree.setState(TreeState.NOT_PRESENT);
            } else {
                tree.setState(TreeState.PRESENT);
                tree.knownLogPositions = new HashSet<>(connectedLogs);
            }
        }

    }

    private static Tree findSecondNearestSmallTree(BlockPos playerPos) {
        List<Tree> sortedTrees = new ArrayList<>();

        for (Tree tree : trees.values()) {
            if (tree.isBig() || !isTreeAllowed(tree) || !shouldWaypointBeVisible(tree) || (tree.getState() == TreeState.REGENERATING && tree.getTimeInCurrentState() <= tree.getRegeneratingDuration() - 2000)) {
                continue;
            }
            sortedTrees.add(tree);
        }

        sortedTrees.sort((t1, t2) -> {
            double dist1 = playerPos.getSquaredDistance(t1.basePos);
            double dist2 = playerPos.getSquaredDistance(t2.basePos);
            return Double.compare(dist1, dist2);
        });

        return sortedTrees.size() >= 2 ? sortedTrees.get(1) : null;
    }

}
