package com.skyblock21.gui;

import com.skyblock21.Skyblock21;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.*;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class FontManager {

    private static final HashMap<String, TextRenderer> FONT_CACHE = new HashMap<>();

    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static ResourceManager RESOURCE_MANAGER = MC.getResourceManager();
    private static TextureManager TEXTURE_MANAGER = MC.getTextureManager();

    public static TextRenderer getFont(final String fontName, final int size) {
        if (RESOURCE_MANAGER == null || TEXTURE_MANAGER == null) {
            RESOURCE_MANAGER = MC.getResourceManager();
            TEXTURE_MANAGER = MC.getTextureManager();
            return MC.textRenderer;
        }

        final String key = (fontName + size).toLowerCase();

        if(FONT_CACHE.containsKey(key))
            return FONT_CACHE.get(key);

        final TrueTypeFontLoader loader = new TrueTypeFontLoader(
                Identifier.of(Skyblock21.MOD_ID, fontName + ".ttf"),
                size, 2.0F,
                TrueTypeFontLoader.Shift.NONE,
                ""
        );

        try {
            final FontLoader.Loadable loadable = loader.build().orThrow();
            final Font font = loadable.load(RESOURCE_MANAGER);

            final Identifier storageId = Identifier.of(Skyblock21.MOD_ID, String.format("%s_font", key));
            final FontStorage storage = new FontStorage(TEXTURE_MANAGER, storageId);

            storage.setFonts(
                    List.of(new Font.FontFilterPair(font, FontFilterType.FilterMap.NO_FILTER)),
                    Collections.emptySet()
            );

            final TextRenderer renderer = new TextRenderer(id -> storage, true);
            FONT_CACHE.put(key, renderer);
            return renderer;
        } catch (final IOException exception) {
            exception.printStackTrace(System.err);
            return MC.textRenderer;
        }
    }
}
