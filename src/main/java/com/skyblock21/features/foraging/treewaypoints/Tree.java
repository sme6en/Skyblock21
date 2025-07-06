package com.skyblock21.features.foraging.treewaypoints;

import com.google.gson.annotations.Expose;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Block;

import java.util.*;

public class Tree {
    public BlockPos basePos;
    @Expose
    public final int x;
    @Expose
    public final int y;
    @Expose
    public final int z;
    @Expose
    public final int maxY;
    @Expose
    public final boolean isBigTree;
    public final boolean isMangrove;
    public UUID waypointId;

    public TreeState currentState = TreeState.NOT_PRESENT;
    public long stateStartTime = 0;
    public Set<BlockPos> knownLogPositions = new HashSet<>();

    public final long NOT_PRESENT_DURATION = 30000;
    public final long REGENERATING_DURATION_SMALL = 13000;
    public final long REGENERATING_DURATION_BIG = 25000;

    public Tree(BlockPos basePos, int maxY, boolean isBigTree, boolean isMangrove) {
        this.basePos = basePos;
        this.x = basePos.getX();
        this.y = basePos.getY();
        this.z = basePos.getZ();
        this.maxY = maxY;
        this.isBigTree = isBigTree;
        this.isMangrove = isMangrove;
        this.waypointId = UUID.randomUUID();
        this.stateStartTime = System.currentTimeMillis();
    }

    public long getRegeneratingDuration() {
        return isBigTree ? REGENERATING_DURATION_BIG : REGENERATING_DURATION_SMALL;
    }

    public long getTimeInCurrentState() {
        return System.currentTimeMillis() - stateStartTime;
    }

    public void setState(TreeState newState) {
        if (this.currentState != newState) {
            this.currentState = newState;
            this.stateStartTime = System.currentTimeMillis();
        }
    }

    public TreeState getState() {
        return currentState;
    }

    public boolean isBig() {
        return isBigTree;
    }

    public boolean isMangrove() {
        return isMangrove;
    }

    public BlockPos getCenterPos() {
        return knownLogPositions.isEmpty() ? basePos :
                findClosestLogToBase(knownLogPositions, basePos);
    }

    public int getLogCount() {
        return knownLogPositions.size();
    }

    public Block getLogBlock() {
        return isMangrove ? Blocks.MANGROVE_WOOD : Blocks.STRIPPED_SPRUCE_WOOD;
    }

    public String getTreeTypeName() {
        StringBuilder typeName = new StringBuilder();

        String colorChar = currentState == TreeState.REGENERATING ? "§6" : "§e";

        if (isBigTree) {
            typeName.append("§c§lL ");
        } else {
            typeName.append("§a§lS ");
        }

        if (isMangrove) {
            typeName.append(colorChar + "Mangrove");
        } else {
            typeName.append(colorChar + "Fig");
        }

        return typeName.toString();
    }

    public long getTimeInState() {
        return getTimeInCurrentState();
    }

    private BlockPos findClosestLogToBase(Set<BlockPos> logPositions, BlockPos basePos) {
        return logPositions.stream()
                           .min((pos1, pos2) -> {
                               double dist1 = basePos.getSquaredDistance(pos1);
                               double dist2 = basePos.getSquaredDistance(pos2);
                               return Double.compare(dist1, dist2);
                           })
                           .orElse(basePos);
    }
}
