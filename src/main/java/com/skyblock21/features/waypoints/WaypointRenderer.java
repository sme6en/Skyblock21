package com.skyblock21.features.waypoints;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.*;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.List;

public class WaypointRenderer {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final double MAX_RENDER_DISTANCE = 1000.0;
    private static final float BEACON_HEIGHT = 256.0f;
    private static final BufferAllocator ALLOCATOR = new BufferAllocator(1536);

    public static final RenderLayer.MultiPhase FILLED_BOX = RenderLayer.of("filled_box", RenderLayer.DEFAULT_BUFFER_SIZE, false, false, RenderPipelines.DEBUG_FILLED_BOX, RenderLayer.MultiPhaseParameters.builder()
                                                                                                                                                                                                         .build(false));

    public static void renderWaypoints(WorldRenderContext context, Camera camera, float tickDelta) {
        if (client.world == null || client.player == null) return;

        List<Waypoint> visibleWaypoints = WaypointManager.getVisibleWaypoints();

        if (visibleWaypoints.isEmpty()) return;

        Vec3d cameraPos = camera.getPos();
        MatrixStack matrices = context.matrixStack();

        if (matrices == null) return;

        for (Waypoint waypoint : visibleWaypoints) {
            Vec3d waypointPos = Vec3d.of(waypoint.getPosition());
            double distance = Math.sqrt(client.player.squaredDistanceTo(waypointPos));
            if (waypoint.shouldHideWhenClose() && distance < 10.0) continue;

            if (distance > MAX_RENDER_DISTANCE) continue;

            if (waypoint.isCircleOnFloor()) {
                renderCircleOnBlock(context, matrices, waypoint.getPosition(), cameraPos, waypoint.getColor(), 0.7f, 256);
            }
            if (waypoint.isBeaconBeam()) {
                renderWaypointBeacon(context, matrices, waypoint, camera, cameraPos, distance);
        }
            if (!waypoint.getName().isEmpty()) renderWaypointText(context, waypoint, distance, tickDelta);

        }

    }

    private static void renderCircleOnBlock(WorldRenderContext context, MatrixStack matrices, BlockPos blockPos, Vec3d cameraPos, int color, float radius, int segments) {
        float alpha = 0.7f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        matrices.push();

        // Position on top of the block
        matrices.translate(
                blockPos.getX() + 0.5 - cameraPos.x,
                blockPos.getY() + 1.01 - cameraPos.y, // Slightly above block surface
                blockPos.getZ() + 0.5 - cameraPos.z
        );

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumerProvider.Immediate consumers = (VertexConsumerProvider.Immediate) context.consumers();
        VertexConsumer vertexConsumer = consumers.getBuffer(FILLED_BOX);

        // Draw circle as triangle fan
        float angleStep = (float)(2 * Math.PI / segments);

        // Center vertex
        float centerX = 0.0f;
        float centerZ = 0.0f;

        for (int i = 0; i <= segments; i++) {
            float angle1 = i * angleStep;
            float angle2 = (i + 1) * angleStep;

            float x1 = centerX + radius * (float)Math.cos(angle1);
            float z1 = centerZ + radius * (float)Math.sin(angle1);
            float x2 = centerX + radius * (float)Math.cos(angle2);
            float z2 = centerZ + radius * (float)Math.sin(angle2);

            // Triangle: center -> point1 -> point2
            vertexConsumer.vertex(matrix, centerX, 0, centerZ).color(r, g, b, alpha)
                          .texture(0.5f, 0.5f).overlay(OverlayTexture.DEFAULT_UV)
                          .light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(0, 1, 0);

            vertexConsumer.vertex(matrix, x1, 0, z1).color(r, g, b, alpha)
                          .texture(0.5f + x1/radius*0.5f, 0.5f + z1/radius*0.5f).overlay(OverlayTexture.DEFAULT_UV)
                          .light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(0, 1, 0);

            vertexConsumer.vertex(matrix, x2, 0, z2).color(r, g, b, alpha)
                          .texture(0.5f + x2/radius*0.5f, 0.5f + z2/radius*0.5f).overlay(OverlayTexture.DEFAULT_UV)
                          .light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(0, 1, 0);

            // Complete the quad by duplicating the last vertex
            vertexConsumer.vertex(matrix, centerX, 0, centerZ).color(r, g, b, alpha)
                          .texture(0.5f, 0.5f).overlay(OverlayTexture.DEFAULT_UV)
                          .light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(0, 1, 0);
        }

        consumers.draw();
        matrices.pop();
    }

    private static void renderWaypointBeacon(WorldRenderContext context, MatrixStack matrices, Waypoint waypoint, Camera camera, Vec3d cameraPos, double distance) {
        BlockPos pos = waypoint.getPosition();
        Vec3d waypointPos = Vec3d.of(pos).add(0.5, 0.0, 0.5);

        matrices.push();
        matrices.translate(
                waypointPos.x - cameraPos.x,
                waypointPos.y - cameraPos.y,
                waypointPos.z - cameraPos.z
        );

        // Render beacon beam
        renderBeaconBeam(context, matrices, waypoint.getColor(), distance);

        matrices.pop();
    }

    private static void renderBeaconBeam(WorldRenderContext context, MatrixStack matrices, int color, double distance) {
        float alpha = Math.max(0.1f, 1.0f - (float)(distance / MAX_RENDER_DISTANCE));

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        // Get vertex consumer for translucent rendering
        VertexConsumerProvider.Immediate consumers = (VertexConsumerProvider.Immediate) context.consumers();
        VertexConsumer vertexConsumer = consumers.getBuffer(FILLED_BOX);

        // Render vertical beam strips
        float width = 0.2f;
        for (int y = 0; y < BEACON_HEIGHT; y += 4) {
            float yPos = y;
            float nextY = Math.min(y + 4, BEACON_HEIGHT);

            // Front face
            vertexConsumer.vertex(matrix, -width, yPos, -width).color(r, g, b, alpha);
            vertexConsumer.vertex(matrix, width, yPos, -width).color(r, g, b, alpha);
            vertexConsumer.vertex(matrix, width, nextY, -width).color(r, g, b, alpha);
            vertexConsumer.vertex(matrix, -width, nextY, -width).color(r, g, b, alpha);

            // Back face
            vertexConsumer.vertex(matrix, width, yPos, width).color(r, g, b, alpha);
            vertexConsumer.vertex(matrix, -width, yPos, width).color(r, g, b, alpha);
            vertexConsumer.vertex(matrix, -width, nextY, width).color(r, g, b, alpha);
            vertexConsumer.vertex(matrix, width, nextY, width).color(r, g, b, alpha);

            // Left face
            vertexConsumer.vertex(matrix, -width, yPos, width).color(r, g, b, alpha);
            vertexConsumer.vertex(matrix, -width, yPos, -width).color(r, g, b, alpha);
            vertexConsumer.vertex(matrix, -width, nextY, -width).color(r, g, b, alpha);
            vertexConsumer.vertex(matrix, -width, nextY, width).color(r, g, b, alpha);

            // Right face
            vertexConsumer.vertex(matrix, width, yPos, -width).color(r, g, b, alpha);
            vertexConsumer.vertex(matrix, width, yPos, width).color(r, g, b, alpha);
            vertexConsumer.vertex(matrix, width, nextY, width).color(r, g, b, alpha);
            vertexConsumer.vertex(matrix, width, nextY, -width).color(r, g, b, alpha);
        }

        consumers.draw();
    }

    private static void renderWaypointText(WorldRenderContext context, Waypoint waypoint, double distance, float tickDelta) {
        if (distance > 250.0) return; // Don't render text for distant waypoints
        Matrix4f positionMatrix = new Matrix4f();

        BlockPos pos = waypoint.getPosition();
        Vec3d waypointPos = Vec3d.of(pos).add(0.5, 2.5, 0.5);


        float scale = 1.0f + (3.0f * (float)(distance / 30));

        scale *= 0.025f;


        TextRenderer textRenderer = client.textRenderer;
        String text = waypoint.getName() + " §c" + Math.round(distance) + "m";
        Camera camera = context.camera();
        Vec3d cameraPos = camera.getPos();

        positionMatrix
                .translate((float) (waypointPos.getX() - cameraPos.getX()), (float) (waypointPos.getY() - cameraPos.getY()), (float) (waypointPos.getZ() - cameraPos.getZ()))
                .rotate(camera.getRotation())
                .scale(scale, -scale, scale);

        VertexConsumerProvider.Immediate consumers = VertexConsumerProvider.immediate(ALLOCATOR);

        float xOffset = -textRenderer.getWidth(text) / 2f;

        textRenderer.draw(text, xOffset, 0, 0xFFFFFF, true, positionMatrix,
                consumers, TextRenderer.TextLayerType.SEE_THROUGH, new Color(0,0,0,130).getRGB(), LightmapTextureManager.MAX_LIGHT_COORDINATE);
        consumers.draw();
    }
}