package com.skyblock21.hud.elements;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.hud.HudElement;
import com.skyblock21.util.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import static com.skyblock21.util.Render2DUtil.DEFAULT_SKULL_SIZE;

public class DarkAuctionTimerElement extends HudElement {

    private static final String DARK_AUCTION_TEXTURE = "eyJ0aW1lc3RhbXAiOjE1NDU3NzE0NDUyNjEsInByb2ZpbGVJZCI6IjkxZjA0ZmU5MGYzNjQzYjU4ZjIwZTMzNzVmODZkMzllIiwicHJvZmlsZU5hbWUiOiJTdG9ybVN0b3JteSIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2FiODM4NThlYmM4ZWU4NWMzZTU0YWIxM2FhYmZjYzFlZjJhZDQ0NmQ2YTkwMGU0NzFjM2YzM2I3ODkwNmE1YiJ9fX0=";
    private final ItemStack darkAuctionSkull;

    public DarkAuctionTimerElement(int x, int y) {
        super(x, y);
        GameProfile profile = new GameProfile(UUID.randomUUID(), "Dark_Auction_Timer");
        profile.getProperties().put("textures", new Property("textures", DARK_AUCTION_TEXTURE));
        darkAuctionSkull = new ItemStack(Items.PLAYER_HEAD);
        darkAuctionSkull.set(DataComponentTypes.PROFILE, new ProfileComponent(profile));
    }

    @Override
    protected void renderElement(DrawContext context) {
        context.drawItem(darkAuctionSkull, 2, 0);
        context.drawText(MinecraftClient.getInstance().textRenderer, getTimeLeft(), DEFAULT_SKULL_SIZE + 4, 4, 0xFFFFFF, true);
    }

    @Override
    public int getWidth() {
        return DEFAULT_SKULL_SIZE + MinecraftClient.getInstance().textRenderer.getWidth(getTimeLeft()) + 8;
    }

    @Override
    public int getHeight() {
        return Math.max(MinecraftClient.getInstance().textRenderer.fontHeight, DEFAULT_SKULL_SIZE) + 4;
    }

    @Override
    public boolean isEnabled() {
        return Skyblock21ConfigManager.get().general.darkAuctionTimer && MinecraftClient.getInstance().currentScreen == null || !(MinecraftClient.getInstance().currentScreen instanceof HandledScreen);
    }

    private String getTimeLeft() {
        Calendar nextDarkAuction = Calendar.getInstance(TimeZone.getTimeZone("EST"));
        if (nextDarkAuction.get(Calendar.MINUTE) >= 55) {
            nextDarkAuction.add(Calendar.HOUR_OF_DAY, 1);
        }
        nextDarkAuction.set(Calendar.MINUTE, 55);
        nextDarkAuction.set(Calendar.SECOND, 0);

        String colorCode = "§а";
        long difference = nextDarkAuction.getTimeInMillis() - System.currentTimeMillis();

        if (difference < 3 * 60 * 1000) {
            colorCode = "§c";
        } else if (difference < 10 * 60 * 1000) {
            colorCode = "§e";
        }

        return colorCode + Utils.formatTime(difference);
    }

}
