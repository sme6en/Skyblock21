package com.skyblock21.config.categories;

import com.skyblock21.config.Skyblock21Config;
import com.skyblock21.features.foraging.GalateaTracker;
import com.skyblock21.features.foraging.treewaypoints.TreeWaypoints;
import com.skyblock21.features.waypoints.WaypointManager;
import com.skyblock21.util.Location;
import com.skyblock21.util.Utils;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;

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
                                               .option(Option.<Boolean>createBuilder()
                                                             .name(literal("Enable Bonus gifts Tracker"))
                                                             .description(OptionDescription.of(literal("Tracks your bonus gifts from Galatea and displays it on screen")))
                                                             .binding(defaults.foraging.bonusGiftsTracker, () -> config.foraging.bonusGiftsTracker, newValue -> config.foraging.bonusGiftsTracker = newValue)
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
                                               .option(ButtonOption.createBuilder()
                                                                   .name(literal("Reset Bonus Gift Tracker"))
                                                                   .description(OptionDescription.of(literal("Resets the bonus gifts tracker data")))
                                                                   .text(literal("Reset"))
                                                                   .action((screen, opt) -> GalateaTracker.resetBonusGifts())
                                                                   .build())
                                               .build())
                             .group(OptionGroup.createBuilder()
                                               .name(literal("Galatea Waypoints"))
                                               .option(Option.<Boolean>createBuilder()
                                                             .name(literal("Enable Waypoints"))
                                                             .description(OptionDescription.of(literal("Shows tree waypoints")))
                                                             .binding(defaults.foraging.treeWaypoints, () -> config.foraging.treeWaypoints, (newValue) -> {
                                                                 config.foraging.treeWaypoints = newValue;
                                                                 if (Utils.getLocation() == Location.GALATEA) {
                                                                     TreeWaypoints.trees.clear();
                                                                     WaypointManager.removeAllWaypoints();
                                                                     TreeWaypoints.performInitialWorldScan();
                                                                 }
                                                             })
                                                             .controller((opt) -> BooleanControllerBuilder.create(opt)
                                                                                                          .yesNoFormatter()
                                                                                                          .coloured(true))
                                                             .build())
                                               .option(Option.<Boolean>createBuilder()
                                                             .name(literal("Fig Trees"))
                                                             .description(OptionDescription.of(literal("Show waypoints for Fig trees")))
                                                             .binding(defaults.foraging.showFigTreeWaypoints, () -> config.foraging.showFigTreeWaypoints, newValue -> config.foraging.showFigTreeWaypoints = newValue)
                                                             .controller((opt) -> BooleanControllerBuilder.create(opt)
                                                                                                          .yesNoFormatter()
                                                                                                          .coloured(true))
                                                             .build())
                                               .option(Option.<Boolean>createBuilder()
                                                             .name(literal("Mangrove Trees"))
                                                             .description(OptionDescription.of(literal("Show waypoints for Mangrove trees")))
                                                             .binding(defaults.foraging.showMangroveTreeWaypoints, () -> config.foraging.showMangroveTreeWaypoints, newValue -> config.foraging.showMangroveTreeWaypoints = newValue)
                                                             .controller((opt) -> BooleanControllerBuilder.create(opt)
                                                                                                          .yesNoFormatter()
                                                                                                          .coloured(true))
                                                             .build())
                                               .option(Option.<Boolean>createBuilder()
                                                             .name(literal("Only Small Trees"))
                                                             .description(OptionDescription.of(literal("Only show waypoints for small trees")))
                                                             .binding(defaults.foraging.onlyShowSmallTrees, () -> config.foraging.onlyShowSmallTrees, newValue -> config.foraging.onlyShowSmallTrees = newValue)
                                                             .controller((opt) -> BooleanControllerBuilder.create(opt)
                                                                                                          .yesNoFormatter()
                                                                                                          .coloured(true))
                                                             .build())
                                               .option(Option.<Integer>createBuilder()
                                                             .name(literal("Time before ready"))
                                                             .description(OptionDescription.of(literal("How long before the tree finishes regeneration and waypoint is shown")))
                                                             .binding(defaults.foraging.timeBeforeReady,
                                                                     () -> config.foraging.timeBeforeReady,
                                                                     newValue -> config.foraging.timeBeforeReady = newValue)
                                                             .controller((opt) -> IntegerSliderControllerBuilder.create(opt)
                                                                                                                .range(0, 12)
                                                                                                                .step(1))
                                                             .build())
                                               .option(Option.<Integer>createBuilder()
                                                             .name(literal("Max Distance"))
                                                             .description(OptionDescription.of(literal("Maximum distance from the player to show waypoints (0 = no limit)")))
                                                             .binding(defaults.foraging.maxDistance,
                                                                     () -> config.foraging.maxDistance,
                                                                     newValue -> config.foraging.maxDistance = newValue)
                                                             .controller((opt) -> IntegerSliderControllerBuilder.create(opt)
                                                                                                                .range(0, 200)
                                                                                                                .step(5))
                                                             .build())
                                     .option(Option.<Boolean>createBuilder()
                                             .name(literal("No beacon beams"))
                                             .description(OptionDescription.of(literal("Hide beacon beams from waypoints")))
                                             .binding(defaults.foraging.noBeaconBeams, () -> config.foraging.noBeaconBeams, newValue -> config.foraging.noBeaconBeams = newValue)
                                             .controller((opt) -> BooleanControllerBuilder.create(opt)
                                                     .yesNoFormatter()
                                                     .coloured(true))
                                             .build())
                                     .option(Option.<Boolean>createBuilder()
                                             .name(literal("Show only nearest"))
                                             .description(OptionDescription.of(literal("Shows only waypoint to the nearest non cut tree")))
                                             .binding(defaults.foraging.onlyNearestTree, () -> config.foraging.onlyNearestTree, newValue -> config.foraging.onlyNearestTree = newValue)
                                             .controller((opt) -> BooleanControllerBuilder.create(opt)
                                                     .yesNoFormatter()
                                                     .coloured(true))
                                             .build())
                                     .build())
                             .option(Option.<Boolean>createBuilder()
                                           .name(literal("Prevent log stripping"))
                                           .description(OptionDescription.of(literal("Prevents from trying to strip logs while in Skyblock")))
                                           .binding(defaults.foraging.preventLogStripping, () -> config.foraging.preventLogStripping, newValue -> config.foraging.preventLogStripping = newValue)
                                           .controller((opt) -> BooleanControllerBuilder.create(opt)
                                                                                        .yesNoFormatter()
                                                                                        .coloured(true))
                                           .build())
                             .option(Option.<Boolean>createBuilder()
                                           .name(literal("Hide floating blocks"))
                                           .description(OptionDescription.of(literal("Hides all the flying blocks while breaking trees")))
                                           .binding(defaults.foraging.hideFloatingBlocks, () -> config.foraging.hideFloatingBlocks, newValue -> config.foraging.hideFloatingBlocks = newValue)
                                           .controller((opt) -> BooleanControllerBuilder.create(opt)
                                                                                        .yesNoFormatter()
                                                                                        .coloured(true))
                                           .build())
                             .build();
    }
}
