package com.skyblock21.config.categories;

import com.skyblock21.config.Skyblock21Config;
import com.skyblock21.features.foraging.GalateaTracker;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.text.Text;

import static net.minecraft.text.Text.literal;

public class ForagingCategory {

    public static ConfigCategory create(Skyblock21Config defaults, Skyblock21Config config) {
        return ConfigCategory.createBuilder()
                             .name(literal("Foraging"))
                             .group(OptionGroup.createBuilder().name(literal("Galatea Tracker"))
                                               .option(Option.<Boolean>createBuilder()
                                                             .name(literal("Enable Tracker"))
                                                             .description(OptionDescription.of(literal("Tracks your foraging whispers rate per hour and displays it on screen")))
                                                             .binding(defaults.foraging.galateaTracker,
                                                                     () -> config.foraging.galateaTracker,
                                                                     newValue -> config.foraging.galateaTracker = newValue)
                                                             .controller((opt) -> BooleanControllerBuilder.create(opt)
                                                                                                          .yesNoFormatter()
                                                                                                          .coloured(true))
                                                             .build())
                                               .option(Option.<Integer>createBuilder()
                                                             .name(literal("Tracker AFK Timeout"))
                                                             .description(OptionDescription.of(literal("Time in seconds after which the tracker considers you AFK")))
                                                             .binding(defaults.foraging.afkTimeout,
                                                                     () -> config.foraging.afkTimeout,
                                                                     newValue -> config.foraging.afkTimeout = newValue)
                                                             .controller((opt) -> IntegerSliderControllerBuilder.create(opt)
                                                                                                                .range(1, 300)
                                                                                                                .step(1))
                                                             .build())
                                               .option(ButtonOption.createBuilder()
                                                                   .name(literal("Reset Tracker"))
                                                                   .description(OptionDescription.of(literal("Resets the galatea tracker data")))
                                                                   .text(literal("Reset"))
                                                                   .action((screen, opt) -> GalateaTracker.resetSession())
                                                                   .build())
                                               .build()).build();
    }
}
