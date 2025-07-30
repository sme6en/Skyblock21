package com.skyblock21.features;

import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.events.EntityEvents;
import com.skyblock21.util.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RunicMobsHighlight {

    private static ConcurrentMap<Integer, Entity> runicEntities = new ConcurrentHashMap<>();

    public static void init() {

        TickSchedulerHelper.repeat(RunicMobsHighlight::onTick, 5);
        EntityEvents.REMOVE.register(RunicMobsHighlight::onEntityRemove);
        WorldRenderEvents.AFTER_ENTITIES.register(RunicMobsHighlight::renderRunicBox);

    }

    private static void onTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (!Utils.isOnSkyblock()) return;
        if (!Skyblock21ConfigManager.get().general.runicMobHighlight) return;

        for (Entity entity : client.world.getEntities()) {
            if (!isRunicMobName(entity)) continue;

            Entity originalEntity = client.world.getOtherEntities(entity, entity.getBoundingBox().expand(0.2, 1, 0.2)).stream().findFirst().orElse(null);
            if (originalEntity == null) continue;

            runicEntities.put(originalEntity.getId(), originalEntity);
        }
    }

    private static boolean isRunicMobName(Entity entity) {
        return entity instanceof ArmorStandEntity && entity.hasCustomName() && TextUtils.toLegacy(entity.getCustomName()).contains("§5]");
    }

    private static void renderRunicBox(WorldRenderContext worldRenderContext) {
        if (!Utils.isOnSkyblock()) return;
        if (!Skyblock21ConfigManager.get().general.runicMobHighlight) return;

        runicEntities.entrySet().removeIf(entry ->
                entry.getValue() == null || entry.getValue().isRemoved() || !entry.getValue().isAlive());

        for (Map.Entry<Integer, Entity> e : runicEntities.entrySet()) {
            MinecraftClient client = MinecraftClient.getInstance();
            Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
            MatrixStack matrices = worldRenderContext.matrixStack();


            Render3DUtil.renderEntityFilledBox(worldRenderContext, matrices, e.getValue(), cameraPos, ColorUtil.getIntFromColor(new Color(155, 0, 255, 255)), 0.7f, 0.3f);
        }
    }

    private static void onEntityRemove(int entityId) {
        runicEntities.remove(entityId);
    }
}
