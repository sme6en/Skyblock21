package com.skyblock21.config.persistent;

import com.google.gson.FieldNamingPolicy;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.nio.file.Path;

public class PersistentData {

    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("skyblock21_data.json");
    private static final ConfigClassHandler<Data> HANDLER = ConfigClassHandler.createBuilder(Data.class)
                                                                                        .id(Identifier.of("skyblock21", "data"))
                                                                                        .serializer(config -> GsonConfigSerializerBuilder.create(config)
                                                                                                                                         .setPath(PATH)
                                                                                                                                         .setJson5(false)
                                                                                                                                         .appendGsonBuilder(builder -> builder.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY))
                                                                                                                                         .build())
                                                                                        .build();

    public static Data get() {
        return HANDLER.instance();
    }

    public static void save() {
        HANDLER.save();
    }

    public static void init() {
        HANDLER.load();
    }
}
