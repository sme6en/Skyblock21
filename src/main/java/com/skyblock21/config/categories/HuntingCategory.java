package com.skyblock21.config.categories;

import com.skyblock21.config.Skyblock21Config;
import com.skyblock21.features.foraging.treewaypoints.TreeWaypoints;
import com.skyblock21.features.waypoints.WaypointManager;
import com.skyblock21.util.Location;
import com.skyblock21.util.Utils;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;

import static net.minecraft.text.Text.literal;

public class HuntingCategory {

    public static ConfigCategory create(Skyblock21Config defaults, Skyblock21Config config) {
        return ConfigCategory.createBuilder()
                .name(literal("Hunting"))
                .option(Option.<Boolean>createBuilder()
                        .name(literal("Hunting tracker"))
                        .description(OptionDescription.of(literal("Tracks hunting exp and gained shards")))
                        .binding(defaults.hunting.huntingTracker, () -> config.hunting.huntingTracker, newValue -> config.hunting.huntingTracker = newValue)
                        .controller((opt) -> BooleanControllerBuilder.create(opt)
                                .yesNoFormatter()
                                .coloured(true))
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(literal("Spin Helper"))
                        .description(OptionDescription.of(literal("Draws a green box around cod fish when able to spin them")))
                        .binding(defaults.hunting.spinHelper, () -> config.hunting.spinHelper, newValue -> config.hunting.spinHelper = newValue)
                        .controller((opt) -> BooleanControllerBuilder.create(opt)
                                .yesNoFormatter()
                                .coloured(true))
                        .build())
                .build();
    }
}
