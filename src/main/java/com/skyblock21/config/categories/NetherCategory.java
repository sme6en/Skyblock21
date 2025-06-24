package com.skyblock21.config.categories;

import com.skyblock21.config.Skyblock21Config;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;

import static net.minecraft.text.Text.literal;

public class NetherCategory {

    public static ConfigCategory create(Skyblock21Config defaults, Skyblock21Config config) {
        return ConfigCategory.createBuilder().name(literal("Nether"))

                             .group(OptionGroup.createBuilder()
                                               .name(literal("Kuudra"))
                                               .option(Option.<Boolean>createBuilder()
                                                             .name(literal("Supply Helper"))
                                                             .description(OptionDescription.of(literal("Adds waypoints over supplies during the Kuudra fight.")))
                                                             .binding(defaults.nether.kuudraSupplyHelper, () -> config.nether.kuudraSupplyHelper, newValue -> config.nether.kuudraSupplyHelper = newValue)
                                                             .controller((opt) -> BooleanControllerBuilder.create(opt)
                                                                                                          .yesNoFormatter()
                                                                                                          .coloured(true))
                                                             .build())

                                               .build()).build();
    }
}
