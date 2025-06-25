package com.skyblock21.features.foraging;

import com.skyblock21.events.SkyblockEvents;
import com.skyblock21.util.Utils;
import net.azureaaron.hmapi.data.rank.PackageRank;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TreeProgress {

    private static final Pattern TREE_PROGRESS_PATTERN = Pattern.compile("^(?:FIG|MANGROVE) TREE (\\d+)%");

    private static List<Entity> entityList = new ArrayList<>();
    private static Entity currentEntity = null;
    private static boolean isMangrove = false;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(TreeProgress::onTick);
        SkyblockEvents.LOCATION_CHANGE.register((location -> {
            if (!entityList.isEmpty()) entityList.clear();
        }));
        ClientEntityEvents.ENTITY_UNLOAD.register(((entity, clientWorld) -> {
            if (entityList.contains(entity)) {
                entityList.remove(entity);
            }
            if (currentEntity == entity) {
                currentEntity = null;
            }
        }));
    }


    public static void onTick(MinecraftClient client) {
        if (!Utils.isOnSkyblock()) return;
        if (!Utils.isInGalatea()) return;
        Box searchBox = client.player.getBoundingBox().expand(80d);
        ArmorStandEntity armorStand = client.world.getOtherEntities(client.player, searchBox, e -> e instanceof ArmorStandEntity && e.hasCustomName() && TREE_PROGRESS_PATTERN.matcher(e.getCustomName()
                                                                                                                                                                                        .getString())
                                                                                                                                                                              .find())
                                                  .stream()
                                                  .filter(e -> e instanceof ArmorStandEntity)
                                                  .map(e -> (ArmorStandEntity) e)
                                                  .findFirst()
                                                  .orElse(null);
        if (armorStand == null) return;

        Entity isPlayerOwned = armorStand.getEntityWorld()
                                          .getOtherEntities(armorStand, armorStand.getBoundingBox()
                                                                                  .expand(0.2F, 1.5F, 0.2F), e -> e instanceof ArmorStandEntity && (e.hasCustomName() && e.getCustomName()
                                                                                                                                                                         .getString()
                                                                                                                                                                         .startsWith("by") || e.hasCustomName() && e.getCustomName()
                                                                                                                                                                                                                    .getString()
                                                                                                                                                                                                                    .startsWith("and")) &&
                                                  e.getCustomName()
                                                   .getString()
                                                   .endsWith((Utils.getRank() != PackageRank.NONE ? "] " : "") + MinecraftClient.getInstance()
                                                                                                                                .getSession()
                                                                                                                                .getUsername())).stream().findFirst()
                                         .orElse(null);

        if (isPlayerOwned == null) return;

        int distance = (int) armorStand.getPos().distanceTo(client.player.getPos());
        if (currentEntity == null || currentEntity.getPos().distanceTo(client.player.getPos()) > distance ) {
            String customName = armorStand.getCustomName().getString();
            isMangrove = customName.contains("MANGROVE");
            currentEntity = armorStand;
        }
    }

    public static void render(DrawContext context, int x, int y) {
        if (currentEntity == null || !currentEntity.hasCustomName() || !currentEntity.isAlive()) return;

        context.drawItem(new ItemStack(isMangrove ? Items.MANGROVE_WOOD : Items.STRIPPED_SPRUCE_LOG), x, y);
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, currentEntity.getStyledDisplayName().getString().split(" ")[2], x + 16 + 2, y + 5, Color.GREEN.getRGB());
    }
}
