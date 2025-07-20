package com.skyblock21.features;

import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.events.SkyblockEvents;
import com.skyblock21.mixin.accessors.PlayerListHudAccessor;
import com.skyblock21.util.TextUtils;
import com.skyblock21.util.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CookieGodPotReminder {

    private static final Pattern COOKIE_DURATION_REGEX = Pattern.compile("^(\\d+)\\s+(hours|seconds)");

    public static void init() {
        SkyblockEvents.JOIN.register(() -> {
            shouldCheck = true;
            checked = false;
        });

        ClientTickEvents.END_CLIENT_TICK.register(CookieGodPotReminder::onTick);
    }


    public static void checkCookieAndGodPotion() {
        if (!Utils.isOnSkyblock()) return;

        String footerText = ((PlayerListHudAccessor) MinecraftClient.getInstance().inGameHud.getPlayerListHud()).getFooter().getString();
        if (footerText == null || footerText.isEmpty() || !footerText.contains("Cookie Buff")) return;

        String[] footerLines = footerText.split("\n");
        if (footerLines.length < 7) return;
        
        String durationText = footerLines[6];

        checked = true;

        Matcher matcher = COOKIE_DURATION_REGEX.matcher(durationText);
        if (footerText.contains("Not active! Obtain booster cookies from the community") && Skyblock21ConfigManager.get().general.boosterCookieReminder) {
            TextUtils.addMessageWithCommandButton("§cYou don't have an active Booster Cookie!", true, "§aClick here to go to lobby", "/warp hub");
        } else if (matcher.find() && Skyblock21ConfigManager.get().general.boosterCookieReminder) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            if (unit.equals("seconds") || value < Skyblock21ConfigManager.get().general.boosterCookieReminderHours) {
                TextUtils.addMessageWithCommandButton("§cBooster Cookie expires soon! ", true, "§аClick here to buy", "/bz booster");
            }
        }

        if (footerText.contains("No effects active. Drink Potions") && Skyblock21ConfigManager.get().general.godPotReminder) {
            TextUtils.addMessage("§6You don't have a God potion!", true, false);
        }
    }

    private static boolean shouldCheck = false;
    private static boolean checked = false;
    public static void onTick(MinecraftClient client) {
        if (!shouldCheck) return;
        if (checked) return;
        if (!Utils.isOnSkyblock()) return;
        if (MinecraftClient.getInstance().inGameHud == null) return;
        if (((PlayerListHudAccessor) MinecraftClient.getInstance().inGameHud.getPlayerListHud()).getFooter() == null) return;

        String footerText = ((PlayerListHudAccessor) MinecraftClient.getInstance().inGameHud.getPlayerListHud()).getFooter().getString();

        if (footerText.contains("Cookie Buff")) {
            checkCookieAndGodPotion();
        }

    }
}
