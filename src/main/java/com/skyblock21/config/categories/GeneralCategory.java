package com.skyblock21.config.categories;

import com.skyblock21.config.Skyblock21Config;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.text.Text;

public class GeneralCategory {

    public static ConfigCategory create(Skyblock21Config defaults, Skyblock21Config config) {
        return ConfigCategory.createBuilder()
                             .name(Text.literal("General"))
                             .option(Option.<Boolean>createBuilder()
                                           .name(Text.literal("No Fog"))
                                           .binding(defaults.general.noFog,
                                                   () -> config.general.noFog,
                                                   newValue -> config.general.noFog = newValue)
                                           .controller((opt) -> BooleanControllerBuilder.create(opt)
                                                                                        .yesNoFormatter()
                                                                                        .coloured(true))
                                           .build())
                             .option(Option.<Boolean>createBuilder()
                                           .name(Text.literal("No Cursor Reset"))
                                           .binding(defaults.general.noMouseReset,
                                                   () -> config.general.noMouseReset,
                                                   newValue -> config.general.noMouseReset = newValue)
                                           .controller((opt) -> BooleanControllerBuilder.create(opt)
                                                                                        .yesNoFormatter()
                                                                                        .coloured(true))
                                           .build())
                             .option(Option.<Boolean>createBuilder()
                                           .name(Text.literal("Copy to Clipboard RNGs"))
                                           .description(OptionDescription.of(Text.literal("Automatically copies RNG messages to clipboard to flex in chat")))
                                           .binding(defaults.general.copyToClipboardRNGs,
                                                   () -> config.general.copyToClipboardRNGs,
                                                   newValue -> config.general.copyToClipboardRNGs = newValue)
                                           .controller((opt) -> BooleanControllerBuilder.create(opt)
                                                                                        .yesNoFormatter()
                                                                                        .coloured(true))
                                           .build())
                             .group(OptionGroup.createBuilder().name(Text.literal("Reminders"))
                                               .option(Option.<Boolean>createBuilder()
                                                             .name(Text.literal("Booster Cookie reminder"))
                                                             .description(OptionDescription.of(Text.literal("Notifies you when your booster cookie is about to expire or has expired")))
                                                             .binding(defaults.general.boosterCookieReminder,
                                                                     () -> config.general.boosterCookieReminder,
                                                                     newValue -> config.general.boosterCookieReminder = newValue)
                                                             .controller((opt) -> BooleanControllerBuilder.create(opt)
                                                                                                          .yesNoFormatter()
                                                                                                          .coloured(true))
                                                             .build())
                                               .option(Option.<Integer>createBuilder()
                                                             .name(Text.literal("Hours before Booster Cookie reminder"))
                                                             .description(OptionDescription.of(Text.literal("How many hours before your booster cookie expires should you be reminded?")))
                                                             .binding(defaults.general.boosterCookieReminderHours,
                                                                     () -> config.general.boosterCookieReminderHours,
                                                                     newValue -> config.general.boosterCookieReminderHours = newValue)
                                                             .controller((opt) -> IntegerSliderControllerBuilder.create(opt)
                                                                                                                .range(1, 24)
                                                                                                                .step(1))
                                                             .build())
                                               .option(Option.<Boolean>createBuilder()
                                                             .name(Text.literal("Expired God Potion reminder"))
                                                             .description(OptionDescription.of(Text.literal("Notifies you when your god potion has expired")))
                                                             .binding(defaults.general.godPotReminder,
                                                                     () -> config.general.godPotReminder,
                                                                     newValue -> config.general.godPotReminder = newValue)
                                                             .controller((opt) -> BooleanControllerBuilder.create(opt)
                                                                                                          .yesNoFormatter()
                                                                                                          .coloured(true))
                                                             .build())
                                               .build()).build();
    }
}
