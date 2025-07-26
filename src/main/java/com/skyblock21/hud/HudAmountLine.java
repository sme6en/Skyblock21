package com.skyblock21.hud;

import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.text.DecimalFormat;

@Getter
public class HudAmountLine extends HudLine {
    private String itemName;
    private long amount;
    private int customValue;
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,###");

    public HudAmountLine(String id, String itemName, long amount) {
        super(id, Text.literal(itemName + " §fx" + formatAmount(amount)));
        this.itemName = itemName;
        this.amount = amount;
        this.customValue = 0;
    }

    public HudAmountLine(String id, String itemName, long amount, String groupId) {
        super(id, Text.literal(itemName + " §fx" + formatAmount(amount)), groupId);
        this.itemName = itemName;
        this.amount = amount;
        this.customValue = 0;
    }

    public HudAmountLine(String id, String itemName, long amount, String groupId, int order) {
        super(id, Text.literal(itemName + " §fx" + formatAmount(amount)), groupId, order);
        this.itemName = itemName;
        this.amount = amount;
        this.customValue = 0;
    }

    public static HudAmountLine createDummy(String id, String itemName, long amount) {
        HudAmountLine line = new HudAmountLine(id, itemName, amount);
        line.setDummy(true);
        return line;
    }

    public static HudAmountLine createDummy(String id, String itemName, long amount, String groupId) {
        HudAmountLine line = new HudAmountLine(id, itemName, amount, groupId);
        line.setDummy(true);
        return line;
    }

    public static HudAmountLine createDummy(String id, String itemName, long amount, String groupId, int order) {
        HudAmountLine line = new HudAmountLine(id, itemName, amount, groupId, order);
        line.setDummy(true);
        return line;
    }

    public void updateAmount(long newAmount) {
        this.amount = newAmount;
        updateContent();
    }

    public void updateItemName(String newName) {
        this.itemName = newName;
        updateContent();
    }

    public void updateCustomValue(int value) {
        this.customValue = value;
    }

    private void updateContent() {
        setContent(Text.literal(itemName + " x" + formatAmount(amount)));
    }

    public static String formatAmount(long amount) {
        return NUMBER_FORMAT.format(amount);
    }

    @Override
    public int getSortValue() {
        return switch (getCurrentSortType()) {
            case AMOUNT -> (int) Math.min(amount, Integer.MAX_VALUE);
            case NAME_LENGTH -> itemName.length();
            case CUSTOM -> customValue;
            default -> getOrder();
        };
    }

    @Override
    public int getDisplayWidth() {
        return MinecraftClient.getInstance().textRenderer.getWidth(itemName);
    }

    private SortType getCurrentSortType() {
        return SortType.ORDER;
    }
}