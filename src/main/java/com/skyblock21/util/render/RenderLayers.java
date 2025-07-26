package com.skyblock21.util.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.TriState;
import net.minecraft.client.gl.RenderPipelines;

import java.util.OptionalDouble;
import java.util.function.Function;

public class RenderLayers {

    public static final RenderLayer.MultiPhase QUADS_GUI = RenderLayer.of("skyblock21:quads_gui", 786432, false, true,
            Skyblock21RenderPipelines.QUADS_GUI, RenderLayer.MultiPhaseParameters.builder().build(false));

    public static final Function<Identifier, RenderLayer> TEXTURES_QUADS_GUI = Util
            .memoize((Function<Identifier, RenderLayer>) (texture -> RenderLayer.of("skyblock21:textured_quads_gui", 1536,
                    false, false, RenderPipelines.FIRE_SCREEN_EFFECT, RenderLayer.MultiPhaseParameters.builder()
                                                                                                      .texture(new RenderPhase.Texture(texture, TriState.FALSE, false)).build(false))));

    public static final RenderLayer.MultiPhase TRIS_GUI = RenderLayer.of("skyblock21:tris_gui", 786432, false, true,
            Skyblock21RenderPipelines.TRIS_GUI, RenderLayer.MultiPhaseParameters.builder().build(false));

    public static final RenderLayer.MultiPhase LINES_GUI = RenderLayer.of("skyblock21:lines_gui", 786432, false, true,
            Skyblock21RenderPipelines.LINES_GUI, RenderLayer.MultiPhaseParameters.builder()
                                                                           .lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(1))).build(false));

}
