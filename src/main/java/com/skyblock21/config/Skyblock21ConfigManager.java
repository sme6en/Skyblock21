package com.skyblock21.config;

import com.google.gson.FieldNamingPolicy;
import com.skyblock21.config.categories.ForagingCategory;
import com.skyblock21.config.categories.GeneralCategory;
import com.skyblock21.config.categories.MiningCategory;
import com.skyblock21.config.categories.NetherCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.apache.commons.lang3.function.Consumers;

import java.nio.file.Path;
import java.util.function.Consumer;

public class Skyblock21ConfigManager {

    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("skyblock21.json");
    private static final ConfigClassHandler<Skyblock21Config> HANDLER = ConfigClassHandler.createBuilder(Skyblock21Config.class)
                                                                                          .serializer(config -> GsonConfigSerializerBuilder.create(config)
                                                                                                                                           .setPath(PATH)
                                                                                                                                           .setJson5(false)
                                                                                                                                           .appendGsonBuilder(builder -> builder
                                                                                                                                                   .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                                                                                                                                           )
                                                                                                                                           .build())
                                                                                          .build();

    public static Skyblock21Config get() {
        return HANDLER.instance();
    }

    public static void update(Consumer<Skyblock21Config> action) {
        action.accept(get());
        HANDLER.save();
    }

    public static void load() {
        HANDLER.load();
    }

    public static void save() {
        update(Consumers.nop());
    }

    public static Screen createGUI(Screen parent) {
        return YetAnotherConfigLib.create(HANDLER, (defaults, config, builder) -> builder
                .title(Text.literal("Skyblock21 Config"))
                .category(GeneralCategory.create(defaults, config))
                .category(MiningCategory.create(defaults, config))
                .category(ForagingCategory.create(defaults, config))
                .category(NetherCategory.create(defaults, config))).generateScreen(parent);
    }

    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> EnumControllerBuilder<E> createEnumCyclingListController(Option<E> opt) {
        return EnumControllerBuilder.create(opt).enumClass((Class<E>) opt.stateManager().get().getClass());
    }
}
