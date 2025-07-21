package com.skyblock21.util;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import org.joml.Vector4f;
import net.minecraft.util.Identifier;
import me.x150.renderer.render.ExtendedDrawContext;
import me.x150.renderer.util.Color;

public class Render2DUtil {

    private static final MinecraftClient MC = MinecraftClient.getInstance();

    public static void drawRoundedRect(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            Vector4f radius,
            int color
    ) {

        ExtendedDrawContext.drawRoundedRect(
                context,
                x,
                y,
                width,
                height,
                radius,
                new Color(ColorUtil.getColorFromInt(color))
        );
    }

    public static void drawRoundedRectOutline(DrawContext context, float x, float y, float width, float height, float radius, float lineWidth, int color) {
        int ix = (int) x;
        int iy = (int) y;
        int iWidth = (int) width;
        int iHeight = (int) height;
        int iRadius = Math.min((int) radius, Math.min(iWidth, iHeight) / 2);
        int iLineWidth = Math.max(1, (int) lineWidth);

        if(iRadius <= 0) {
            drawRectOutline(context, ix, iy, iWidth, iHeight, iLineWidth, color);
            return;
        }

        context.fill(ix + iRadius, iy, ix + iWidth - iRadius, iy + iLineWidth, color);
        context.fill(ix + iRadius, iy + iHeight - iLineWidth, ix + iWidth - iRadius, iy + iHeight, color);
        context.fill(ix, iy + iRadius, ix + iLineWidth, iy + iHeight - iRadius, color);
        context.fill(ix + iWidth - iLineWidth, iy + iRadius, ix + iWidth, iy + iHeight - iRadius, color);

        drawCornerOutline(context, ix, iy, iRadius, iLineWidth, color, CornerType.TOP_LEFT);
        drawCornerOutline(context, ix + iWidth - iRadius, iy, iRadius, iLineWidth, color, CornerType.TOP_RIGHT);
        drawCornerOutline(context, ix + iWidth - iRadius, iy + iHeight - iRadius, iRadius, iLineWidth, color, CornerType.BOTTOM_RIGHT);
        drawCornerOutline(context, ix, iy + iHeight - iRadius, iRadius, iLineWidth, color, CornerType.BOTTOM_LEFT);
    }

    private static void drawCorner(DrawContext context, int x, int y, int radius, int color, CornerType corner) {
        int baseAlpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        for(int py = 0; py < radius; py++) {
            for(int px = 0; px < radius; px++) {
                float coverage = getPixelCoverage(px, py, radius, corner);

                if(coverage > 0) {
                    int alpha = (int) (baseAlpha * coverage);
                    if(alpha > 0) {
                        int pixelColor = (alpha << 24) | (red << 16) | (green << 8) | blue;

                        int drawX = x + (corner == CornerType.TOP_RIGHT || corner == CornerType.BOTTOM_RIGHT ? radius - px - 1 : px);
                        int drawY = y + (corner == CornerType.BOTTOM_LEFT || corner == CornerType.BOTTOM_RIGHT ? radius - py - 1 : py);

                        context.fill(drawX, drawY, drawX + 1, drawY + 1, pixelColor);
                    }
                }
            }
        }
    }

    private static void drawCornerOutline(DrawContext context, int x, int y, int radius, int lineWidth, int color, CornerType corner) {
        int baseAlpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        for(int py = 0; py < radius; py++) {
            for(int px = 0; px < radius; px++) {
                float outerCoverage = getPixelCoverage(px, py, radius, corner);
                float innerCoverage = getPixelCoverage(px, py, radius - lineWidth, corner);
                float coverage = Math.max(0, outerCoverage - innerCoverage);

                if(coverage > 0) {
                    int alpha = (int) (baseAlpha * coverage);
                    if(alpha > 0) {
                        int pixelColor = (alpha << 24) | (red << 16) | (green << 8) | blue;

                        int drawX = x + (corner == CornerType.TOP_RIGHT || corner == CornerType.BOTTOM_RIGHT ? radius - px - 1 : px);
                        int drawY = y + (corner == CornerType.BOTTOM_LEFT || corner == CornerType.BOTTOM_RIGHT ? radius - py - 1 : py);

                        context.fill(drawX, drawY, drawX + 1, drawY + 1, pixelColor);
                    }
                }
            }
        }
    }

    private static float getPixelCoverage(int px, int py, float radius, CornerType corner) {
        if(radius <= 0) return 0;

        float centerX = radius - 0.5f;
        float centerY = radius - 0.5f;

        int samples = 0;
        int inside = 0;

        for(int sx = 0; sx < 4; sx++) {
            for(int sy = 0; sy < 4; sy++) {
                float sampleX = px + (sx + 0.5f) / 4.0f;
                float sampleY = py + (sy + 0.5f) / 4.0f;

                float dx = sampleX - centerX;
                float dy = sampleY - centerY;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                if(dist <= radius) {
                    inside++;
                }
                samples++;
            }
        }

        return (float) inside / samples;
    }

    private static void drawRectOutline(DrawContext context, int x, int y, int width, int height, int lineWidth, int color) {
        context.fill(x, y, x + width, y + lineWidth, color);
        context.fill(x, y + height - lineWidth, x + width, y + height, color);
        context.fill(x, y, x + lineWidth, y + height, color);
        context.fill(x + width - lineWidth, y, x + width, y + height, color);
    }

    public static void drawImage(
            Identifier texture,
            int posX,
            int posY,
            int renderWidth,
            int renderHeight,
            int imageWidth,
            int imageHeight,
            DrawContext context
    ) {
        context.drawTexture(
                RenderLayer::getGuiTextured,
                texture,
                posX, posY,
                0, 0,
                renderWidth, renderHeight,
                imageWidth, imageHeight
        );
    }

    public static void drawRoundedRect(DrawContext context, int x, int y, int width, int height, Vector4f radius) {
        drawRoundedRect(context, x, y, width, height, radius, 0xFFFFFFFF);
    }

    public static void drawRoundedRectOutline(DrawContext context, float x, float y, float width, float height, float radius, float lineWidth) {
        drawRoundedRectOutline(context, x, y, width, height, radius, lineWidth, 0xFFFFFFFF);
    }

    public static void drawGradientRoundedRect(DrawContext context, int x, int y, int width, int height, Vector4f radius, int colorLeft, int colorRight) {
        for (int i = 0; i < width; i++) {
            float factor = (float) i / width;
            int interpolatedColor = ColorUtil.interpolateColor(colorLeft, colorRight, factor);

            Vector4f columnRadius = new Vector4f(0, 0, 0, 0);

            if (i == 0) {
                columnRadius.x = radius.x;
                columnRadius.w = radius.w;
            }
            if (i == width - 1) {
                columnRadius.y = radius.y;
                columnRadius.z = radius.z;
            }

            drawRoundedRect(context, x + i, y, 1, height, columnRadius, interpolatedColor);
        }
    }

    public static void drawRect(DrawContext context, float x, float y, float width, float height, int color) {
        context.fill((int) x, (int) y, (int) (x + width), (int) (y + height), color);
    }

    private enum CornerType {
        TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT
    }

}
