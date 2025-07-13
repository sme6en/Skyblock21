package com.skyblock21.util;

import com.skyblock21.features.waypoints.WaypointRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.CodEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class RenderUtil {

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
}