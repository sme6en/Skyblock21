package com.skyblock21.util.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.skyblock21.Skyblock21;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Skyblock21RenderPipelines {

    private static final List<RenderPipeline> PIPELINES = new ArrayList<>();

    public static final RenderPipeline QUADS_GUI = add(RenderPipelines.register(RenderPipeline.builder()
                                                                                          .withVertexShader("core/position_color").withFragmentShader("core/position_color")
                                                                                          .withBlend(BlendFunction.TRANSLUCENT).withBlend(BlendFunction.TRANSLUCENT)
                                                                                          .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS).withCull(false)
                                                                                          .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withLocation("pipeline/skyblock21_quads_gui").build()));

    public static final RenderPipeline TEXTURED_QUADS_GUI = add(RenderPipelines.register(RenderPipeline.builder()
                                                                                                   .withVertexShader("core/position_tex_color").withFragmentShader("core/position_tex_color")
                                                                                                   .withSampler("Sampler0").withBlend(BlendFunction.TRANSLUCENT)
                                                                                                   .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
                                                                                                   .withLocation("pipeline/skyblock21_textured_quads_gui").withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                                                                                                   .withDepthWrite(false).build()));

    public static final RenderPipeline TRIS_GUI = add(RenderPipelines.register(RenderPipeline.builder()
                                                                                         .withVertexShader("core/position_color").withFragmentShader("core/position_color")
                                                                                         .withBlend(BlendFunction.TRANSLUCENT).withBlend(BlendFunction.TRANSLUCENT)
                                                                                         .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLES).withCull(false)
                                                                                         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withLocation("pipeline/skyblock21_tris_gui").build()));

    public static final RenderPipeline LINES_GUI = add(RenderPipelines.register(RenderPipeline.builder()
                                                                                          .withVertexShader("core/position_color").withFragmentShader("core/position_color")
                                                                                          .withBlend(BlendFunction.TRANSLUCENT).withBlend(BlendFunction.TRANSLUCENT)
                                                                                          .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.DEBUG_LINE_STRIP).withCull(false)
                                                                                          .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withLocation("pipeline/skyblock21_lines_gui").build()));

    private static RenderPipeline add(RenderPipeline pipeline) {
        PIPELINES.add(pipeline);
        return pipeline;
    }

    public static void precompile() {
        GpuDevice device = RenderSystem.getDevice();
        ResourceManager resources = MinecraftClient.getInstance().getResourceManager();

        for (RenderPipeline pipeline : PIPELINES) {
            device.precompilePipeline(pipeline, (identifier, shaderType) -> {
                var resource = resources.getResource(identifier).get();

                try (var in = resource.getInputStream()) {
                    return IOUtils.toString(in, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
