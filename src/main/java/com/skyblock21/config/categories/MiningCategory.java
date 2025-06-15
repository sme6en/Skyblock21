package com.skyblock21.config.categories;

import com.skyblock21.config.Skyblock21Config;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;

import static net.minecraft.text.Text.literal;

public class MiningCategory {

    public static ConfigCategory create(Skyblock21Config defaults, Skyblock21Config config) {
        return ConfigCategory.createBuilder().name(literal("Mining"))

                             .group(OptionGroup.createBuilder().name(literal("Scatha Helpers"))
                        .option(Option.<Boolean>createBuilder()
                                      .name(literal("Scatha Alerts"))
                                      .description(OptionDescription.of(literal("Alerts you when a scatha or a worm spawns")))
                                .binding(defaults.mining.scathaAlerts,
                                        () -> config.mining.scathaAlerts,
                                        newValue -> config.mining.scathaAlerts = newValue)
                                .controller((opt) -> BooleanControllerBuilder.create(opt).yesNoFormatter().coloured(true))
                                .build()).option(Option.<Boolean>createBuilder()
                                                       .name(literal("Scatha Tracker"))
                                                       .description(OptionDescription.of(literal("Tracks the scatha and worm spawns, showing you useful info on the HUD.")))
                                                       .binding(defaults.mining.scathaTracker,
                                                               () -> config.mining.scathaTracker,
                                                               newValue -> config.mining.scathaTracker = newValue)
                                                       .controller((opt) -> BooleanControllerBuilder.create(opt)
                                                                                                    .yesNoFormatter()
                                                                                                    .coloured(true))
                                                       .build())
                                               .option(Option.<Boolean>createBuilder()
                                                             .name(literal("Mouse lock keybind"))
                                                             .description(OptionDescription.of(literal("Enabled the mouse lock keybind, which makes the mouse not move. Useful for scatha mining or farming.")))
                                .binding(defaults.mining.mouseLockKeybind,
                                        () -> config.mining.mouseLockKeybind,
                                        newValue -> config.mining.mouseLockKeybind = newValue)
                                .controller((opt) -> BooleanControllerBuilder.create(opt).yesNoFormatter().coloured(true))
                                .build()).build()).build();
    }
}
