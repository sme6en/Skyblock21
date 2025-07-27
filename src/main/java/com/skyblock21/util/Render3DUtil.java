package com.skyblock21.util;

import com.skyblock21.features.waypoints.WaypointRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.CodEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class Render3DUtil {

    /**
     * Renders a filled box using VertexRendering.drawFilledBox method
     * This is a wrapper that converts from Box coordinates to the expected format
     */
    public static void drawFilledBox(MatrixStack matrices, VertexConsumer buffer, Box box,
                                     float red, float green, float blue, float alpha) {
        // Assuming VertexRendering class exists with this method
        VertexRendering.drawFilledBox(matrices, buffer,
                (float) box.minX, (float) box.minY, (float) box.minZ,
                (float) box.maxX, (float) box.maxY, (float) box.maxZ,
                red, green, blue, alpha);
    }

    /**
     * Renders a filled box using VertexRendering.drawFilledBox with direct coordinates
     */
    public static void drawFilledBox(MatrixStack matrices, VertexConsumer buffer,
                                     float minX, float minY, float minZ,
                                     float maxX, float maxY, float maxZ,
                                     float red, float green, float blue, float alpha) {
        VertexRendering.drawFilledBox(matrices, buffer, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
    }

    /**
     * Renders a filled box using VertexRendering.drawFilledBox with color array
     */
    public static void drawFilledBox(MatrixStack matrices, VertexConsumer buffer,
                                     float minX, float minY, float minZ,
                                     float maxX, float maxY, float maxZ,
                                     float[] colorComponents, float alpha) {
        VertexRendering.drawFilledBox(matrices, buffer, minX, minY, minZ, maxX, maxY, maxZ,
                colorComponents[0], colorComponents[1], colorComponents[2], alpha);
    }



    private static void renderBoxOutline(Matrix4f matrix, VertexConsumer vertexConsumer,
                                         float width, float height, float depth,
                                         float r, float g, float b, float alpha) {
        // Bottom face edges
        vertexConsumer.vertex(matrix, 0, 0, 0).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, width, 0, 0).color(r, g, b, alpha);

        vertexConsumer.vertex(matrix, width, 0, 0).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, width, 0, depth).color(r, g, b, alpha);

        vertexConsumer.vertex(matrix, width, 0, depth).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, 0, 0, depth).color(r, g, b, alpha);

        vertexConsumer.vertex(matrix, 0, 0, depth).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, 0, 0, 0).color(r, g, b, alpha);

        // Top face edges
        vertexConsumer.vertex(matrix, 0, height, 0).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, width, height, 0).color(r, g, b, alpha);

        vertexConsumer.vertex(matrix, width, height, 0).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, width, height, depth).color(r, g, b, alpha);

        vertexConsumer.vertex(matrix, width, height, depth).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, 0, height, depth).color(r, g, b, alpha);

        vertexConsumer.vertex(matrix, 0, height, depth).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, 0, height, 0).color(r, g, b, alpha);

        // Vertical edges
        vertexConsumer.vertex(matrix, 0, 0, 0).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, 0, height, 0).color(r, g, b, alpha);

        vertexConsumer.vertex(matrix, width, 0, 0).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, width, height, 0).color(r, g, b, alpha);

        vertexConsumer.vertex(matrix, width, 0, depth).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, width, height, depth).color(r, g, b, alpha);

        vertexConsumer.vertex(matrix, 0, 0, depth).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, 0, height, depth).color(r, g, b, alpha);
    }

    /**
     * Helper method to render a filled box (similar to beacon beam style)
     * Keep this for compatibility or if you prefer the manual vertex approach
     */
    public static void renderFilledBox(Matrix4f matrix, VertexConsumer vertexConsumer,
                                       float width, float height, float depth,
                                       float r, float g, float b, float alpha) {
        // Front face
        vertexConsumer.vertex(matrix, 0, 0, 0).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, width, 0, 0).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, width, height, 0).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, 0, height, 0).color(r, g, b, alpha);

        // Back face
        vertexConsumer.vertex(matrix, width, 0, depth).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, 0, 0, depth).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, 0, height, depth).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, width, height, depth).color(r, g, b, alpha);

        // Left face
        vertexConsumer.vertex(matrix, 0, 0, depth).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, 0, 0, 0).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, 0, height, 0).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, 0, height, depth).color(r, g, b, alpha);

        // Right face
        vertexConsumer.vertex(matrix, width, 0, 0).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, width, 0, depth).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, width, height, depth).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, width, height, 0).color(r, g, b, alpha);

        // Top face
        vertexConsumer.vertex(matrix, 0, height, 0).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, width, height, 0).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, width, height, depth).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, 0, height, depth).color(r, g, b, alpha);

        // Bottom face
        vertexConsumer.vertex(matrix, 0, 0, depth).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, width, 0, depth).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, width, 0, 0).color(r, g, b, alpha);
        vertexConsumer.vertex(matrix, 0, 0, 0).color(r, g, b, alpha);
    }

    private static void renderVertex(MatrixStack.Entry matrix, VertexConsumer vertices, int color, float x, float y, float z) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        vertices.vertex(matrix, x, y, z).color(r, g, b, a).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(0xF000F0).normal(matrix, 0, 1, 0);
    }

    private static void renderVertex(MatrixStack.Entry matrix, VertexConsumer vertices, float x, float y, float z, float u, float v) {
        vertices.vertex(matrix, x, y, z)
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(0xF000F0)
                .normal(matrix, 0, 1, 0);
    }

    private static void renderQuad(MatrixStack.Entry matrix, VertexConsumer vertices, int color, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
        renderVertex(matrix, vertices, color, x1, y1, z1);
        renderVertex(matrix, vertices, color, x2, y2, z2);
        renderVertex(matrix, vertices, color, x3, y3, z3);
        renderVertex(matrix, vertices, color, x4, y4, z4);
    }

    private static void renderBeamQuad(MatrixStack.Entry matrix, VertexConsumer vertices,
                                       float x1, float y1, float z1,
                                       float x2, float y2, float z2,
                                       float x3, float y3, float z3,
                                       float x4, float y4, float z4,
                                       float u1, float v1, float u2, float v2) {
        renderVertex(matrix, vertices, x1, y1, z1, u1, v1);
        renderVertex(matrix, vertices, x2, y2, z2, u2, v1);
        renderVertex(matrix, vertices, x3, y3, z3, u2, v2);
        renderVertex(matrix, vertices, x4, y4, z4, u1, v2);
    }

    public static void renderEntityFilledBox(WorldRenderContext context, MatrixStack matrices,
                                             Entity entity, Vec3d cameraPos,
                                             int color, float alpha) {
        if (entity == null || entity.isRemoved()) return;

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        Box boundingBox = entity.getBoundingBox();

        matrices.push();

        matrices.translate(
                boundingBox.minX - cameraPos.x,
                boundingBox.minY - cameraPos.y,
                boundingBox.minZ - cameraPos.z
        );

        VertexConsumerProvider.Immediate consumers = (VertexConsumerProvider.Immediate) context.consumers();
        if (consumers == null) return;
        VertexConsumer vertexConsumer = consumers.getBuffer(WaypointRenderer.FILLED_BOX);

        drawFilledBox(matrices, vertexConsumer,
                0, 0, 0,
                (float) (boundingBox.maxX - boundingBox.minX),
                (float) (boundingBox.maxY - boundingBox.minY),
                (float) (boundingBox.maxZ - boundingBox.minZ),
                r, g, b, alpha);

        consumers.draw();
        matrices.pop();
    }
}