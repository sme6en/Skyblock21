package com.skyblock21.features;

import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.features.waypoints.WaypointRenderer;
import com.skyblock21.util.Render3DUtil;
import com.skyblock21.util.TextUtils;
import com.skyblock21.util.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.CodEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class SpinHelper {

    private static final List<SpinEntry> spinEntries = new ArrayList<>();
    private static boolean isHoldingFishingNet = false;
    private static int pullAmount = 0;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(SpinHelper::onTick);
        WorldRenderEvents.AFTER_ENTITIES.register(SpinHelper::renderSpinEntities);
    }

    private static void renderSpinEntities(WorldRenderContext worldRenderContext) {
        if (!Utils.isOnSkyblock() || !Utils.isInGalatea() || !isHoldingFishingNet) return;
        if (!Skyblock21ConfigManager.get().hunting.spinHelper) return;

        for (SpinEntry entry : spinEntries) {
            if (!entry.isValidForRendering()) return;

            MinecraftClient client = MinecraftClient.getInstance();
            Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
            MatrixStack matrices = worldRenderContext.matrixStack();

            Render3DUtil.renderEntityFilledBox(worldRenderContext, matrices, entry.spinFish, cameraPos, entry.canSpin(pullAmount) ? 0x00FF00 : 0xFF5555, 0.5f);
        }
    }

    private static void onTick(MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        if (!Utils.isOnSkyblock() || !Utils.isInGalatea()) return;
        if (!Skyblock21ConfigManager.get().hunting.spinHelper) return;

        spinEntries.forEach(SpinEntry::update);
        updateFishingNetState(client);
        updateSpinEntries(client);
        cleanupInvalidEntries();

    }

    private static void updateFishingNetState(MinecraftClient client) {
        ItemStack mainHandItem = client.player.getMainHandStack();

        if (mainHandItem == null || mainHandItem.getItem() != Items.COBWEB) {
            isHoldingFishingNet = false;
            pullAmount = 0;
            return;
        }

        String itemName = mainHandItem.getCustomName() != null ?
                mainHandItem.getCustomName().getString() : "";
        isHoldingFishingNet = itemName.endsWith("Fishing Net");

        if (isHoldingFishingNet) {
            pullAmount = extractPullAmount(mainHandItem);
        }
    }

    private static int extractPullAmount(ItemStack fishingNet) {
        LoreComponent loreComponent = fishingNet.get(DataComponentTypes.LORE);

        if (loreComponent == null || loreComponent.lines().isEmpty()) {
            return 0;
        }

        for (Text loreLine : loreComponent.lines()) {
            if (loreLine.getString().contains("Pull")) {
                String cleanText = TextUtils.removeFormatting(loreLine)
                                            .replace("Pull: +", "")
                                            .trim();
                return (int) Math.ceil(Double.parseDouble(cleanText));
            }
        }

        return 0;
    }

    private static void updateSpinEntries(MinecraftClient client) {
        List<ArmorStandEntity> newSpinArmorStands = findNewSpinArmorStands(client);

        for (ArmorStandEntity spinArmorStand : newSpinArmorStands) {
            SpinEntry newEntry = createSpinEntry(client, spinArmorStand);
            if (newEntry != null) {
                spinEntries.add(newEntry);
            }
        }
    }

    private static List<ArmorStandEntity> findNewSpinArmorStands(MinecraftClient client) {
        Box searchBox = client.player.getBoundingBox()
                                     .expand(18d, 18d, 18d);

        List<ArmorStandEntity> allSpinArmorStands = client.world.getEntitiesByClass(
                ArmorStandEntity.class,
                searchBox,
                SpinHelper::isSpinArmorStand
        );

        return allSpinArmorStands.stream()
                                 .filter(armorStand -> spinEntries.stream()
                                                                  .noneMatch(entry -> entry.armorStand.equals(armorStand)))
                                 .toList();
    }

    private static boolean isSpinArmorStand(ArmorStandEntity armorStand) {
        return armorStand.hasCustomName() &&
                armorStand.getCustomName().getString().contains("« SPIN »");
    }

    private static SpinEntry createSpinEntry(MinecraftClient client, ArmorStandEntity spinArmorStand) {
        ArmorStandEntity percentageArmorStand = findPercentageArmorStand(client, spinArmorStand);
        if (percentageArmorStand == null) return null;

        CodEntity spinFish = findSpinFish(client, percentageArmorStand);
        if (spinFish == null) return null;

        return new SpinEntry(spinArmorStand, percentageArmorStand, spinFish);
    }

    private static ArmorStandEntity findPercentageArmorStand(MinecraftClient client, ArmorStandEntity spinArmorStand) {
        Box searchBox = spinArmorStand.getBoundingBox()
                                      .expand(0.1d, 0.6d, 0.1d);

        return client.world.getEntitiesByClass(
                ArmorStandEntity.class,
                searchBox,
                SpinHelper::isPercentageArmorStand
        ).stream().findFirst().orElse(null);
    }

    private static boolean isPercentageArmorStand(ArmorStandEntity armorStand) {
        return armorStand.hasCustomName() &&
                armorStand.getCustomName().getString().endsWith("%");
    }

    private static CodEntity findSpinFish(MinecraftClient client, ArmorStandEntity percentageArmorStand) {
        Box searchBox = percentageArmorStand.getBoundingBox()
                                            .expand(0.1d, 0.6d, 0.1d);

        return client.world.getEntitiesByClass(CodEntity.class, searchBox, entity -> true)
                           .stream().findFirst().orElse(null);
    }

    private static void cleanupInvalidEntries() {
        spinEntries.removeIf(entry -> !entry.isValid());
    }

    private static class SpinEntry {
        private final ArmorStandEntity armorStand;
        private final ArmorStandEntity spinArmorStand;
        private final CodEntity spinFish;
        public double spinPercentage;

        public SpinEntry(ArmorStandEntity armorStand, ArmorStandEntity spinArmorStand, CodEntity spinFish) {
            this.armorStand = armorStand;
            this.spinArmorStand = spinArmorStand;
            this.spinFish = spinFish;
        }

        public void update() {
            if (spinArmorStand == null || spinFish == null) return;

            String percString = spinArmorStand.getCustomName().getString();
            if (percString.endsWith("%")) {
                try {
                    this.spinPercentage = Double.parseDouble(percString.replace("%", ""));
                } catch (NumberFormatException e) {
                    this.spinPercentage = 0;
                }
            } else {
                this.spinPercentage = 0;
            }
        }

        private boolean canSpin(int spinPercentage) {
            return this.spinPercentage + spinPercentage <= 100;
        }

        public boolean isValid() {
            return armorStand != null && !armorStand.isRemoved() && armorStand.isAlive();
        }

        public boolean isValidForRendering() {
            return isValid() &&
                    spinFish != null &&
                    !spinFish.isRemoved() &&
                    spinFish.isAlive();
        }
    }
}
