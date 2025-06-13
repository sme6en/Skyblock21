package com.skyblock21.config.categories;

import com.skyblock21.config.Skyblock21Config;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import net.minecraft.text.Text;

public class MiningCategory {

    public static ConfigCategory create(Skyblock21Config defaults, Skyblock21Config config) {
        return ConfigCategory.createBuilder().name(Text.literal("Mining"))

                .group(OptionGroup.createBuilder().name(Text.literal("Scatha Helpers"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Scatha Alerts"))
                                .description(OptionDescription.of(Text.literal("Alerts you when a scatha or a worm spawns")))
                                .binding(defaults.mining.scathaAlerts,
                                        () -> config.mining.scathaAlerts,
                                        newValue -> config.mining.scathaAlerts = newValue)
                                .controller((opt) -> BooleanControllerBuilder.create(opt).yesNoFormatter().coloured(true))
                                .build()).option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Mouse lock keybind"))
                                .description(OptionDescription.of(Text.literal("Enabled the mouse lock keybind, which makes the mouse not move. Useful for scatha mining or farming.")))
                                .binding(defaults.mining.mouseLockKeybind,
                                        () -> config.mining.mouseLockKeybind,
                                        newValue -> config.mining.mouseLockKeybind = newValue)
                                .controller((opt) -> BooleanControllerBuilder.create(opt).yesNoFormatter().coloured(true))
                                .build()).build()).build();
    }
}
