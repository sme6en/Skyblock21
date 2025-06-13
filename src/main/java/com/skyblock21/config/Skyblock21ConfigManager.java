package com.skyblock21.config;

import com.skyblock21.config.categories.GeneralCategory;
import com.skyblock21.config.categories.MiningCategory;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.loader.api.FabricLoader;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import com.google.gson.FieldNamingPolicy;
import java.nio.file.Path;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;


public class Skyblock21ConfigManager {

    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("skyblock21.json");
    private static final ConfigClassHandler<Skyblock21Config> HANDLER = ConfigClassHandler.createBuilder(Skyblock21Config.class)
                                                                                          .serializer(config -> GsonConfigSerializerBuilder.create(config)
                                                                                                                                           .setPath(PATH)
                                                                                                                                           .setJson5(false)
                                                                                                                                           .appendGsonBuilder(builder -> builder
                                                                                                                                                   .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY))
                                                                                                                                           .build())
                                                                                          .build();

    public static Skyblock21Config get() {
        return HANDLER.instance();
    }

    public static void load() {
        HANDLER.load();
    }

    public static void save() {
        HANDLER.save();
    }

    public static Screen createGUI(Screen parent) {
        return YetAnotherConfigLib.create(HANDLER, (defaults, config, builder) -> builder
                .title(Text.literal("Skyblock21 Config")).category(GeneralCategory.create(defaults, config)).category(MiningCategory.create(defaults, config))).generateScreen(parent);
    }
}
