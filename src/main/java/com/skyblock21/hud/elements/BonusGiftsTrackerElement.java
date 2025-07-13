package com.skyblock21.hud.elements;

import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.features.foraging.GalateaTracker;
import com.skyblock21.hud.*;
import com.skyblock21.util.Location;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static net.minecraft.text.Text.literal;

public class BonusGiftsTrackerElement extends MultiLineHudElement {

    public static BonusGiftsTrackerElement INSTANCE;
    private Set<String> trackedGifts = new HashSet<>();

    public BonusGiftsTrackerElement(int x, int y) {
        super(x, y, Location.GALATEA);

        if (INSTANCE == null) {
            INSTANCE = this;
        }

        // Set title with purple color and 20% bigger scale
        setTitle("§lBonus Gifts", 0xFF55FF, 1.2f, true);

        // Create group for bonus gifts with amount alignment and sorting by amount (descending)
        createGroup("gifts", "Bonus Gifts", true);
        setGroupSorting("gifts", SortType.AMOUNT, false);

        // Add control lines (only show in containers)
        addContainerClickableLine("reset_gifts",
                literal("§c[Reset Tracker]"),
                () -> {
                    GalateaTracker.resetBonusGifts();
                    clearGiftLines();
                }
        ).setHoverText(literal("§eReset the bonus gifts tracker"));

    }

    private void clearGiftLines() {
        // Remove all gift lines (keep control lines)
        lines.removeIf(line ->
                line.getGroupId() != null && line.getGroupId().equals("gifts"));
        trackedGifts.clear();
        recalculateDimensions();
    }

    public void addOrUpdateGift(String giftName, int amount) {
        String lineId = "gift_" + giftName.hashCode();

        if (trackedGifts.contains(giftName)) {
            updateAmountLine(lineId, "§d" + giftName, amount);
        } else {
            addAmountLine(lineId, "§d" + giftName, amount, "gifts");
            trackedGifts.add(giftName);
        }
    }

    @Override
    protected void onTick(MinecraftClient client) {
        Map<String, Integer> bonusDrops = PersistentData.get().bonusDrops;

        // Add new gifts
        for (Map.Entry<String, Integer> entry : bonusDrops.entrySet()) {
            String giftName = entry.getKey();
            int amount = entry.getValue();
            addOrUpdateGift(giftName, amount);
        }

        // Remove gifts that no longer exist in data
        Set<String> toRemove = new HashSet<>();
        for (String giftName : trackedGifts) {
            if (!bonusDrops.containsKey(giftName)) {
                String lineId = "gift_" + giftName.hashCode();
                removeLine(lineId);
                toRemove.add(giftName);
            }
        }
        trackedGifts.removeAll(toRemove);

        if (!toRemove.isEmpty()) {
            recalculateDimensions();
        }
    }

    @Override
    public boolean shouldRenderDummy() {
        return super.shouldRenderDummy() || PersistentData.get().bonusDrops.isEmpty();
    }

    @Override
    protected void renderDummy(DrawContext context) {
        this.renderElement(context);
    }

    @Override
    public boolean isEnabled() {
        return Skyblock21ConfigManager.get().foraging.bonusGiftsTracker;
    }
}