package com.skyblock21.util.tab;

import com.skyblock21.Skyblock21;
import com.skyblock21.mixin.accessors.PlayerListHudAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

import com.skyblock21.util.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.*;

public class TabUtils {

    private static final Comparator<PlayerListEntry> ORDER = PlayerListHudAccessor.getEntryOrdering();

    public static Set<Widget> activeWidgets;
    public static Map<Widget, List<String>> widgetLines = new EnumMap<>(Widget.class);

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!Utils.isOnSkyblock()) return;
            parseTabList();
        });
    }

    public static void parseTabList() {

        try {
            activeWidgets = null;
            widgetLines.clear();
            boolean inInfoColumn = false;
            Widget currentWidget = null;
            var network = MinecraftClient.getInstance().getNetworkHandler();
            if (network == null) return;
            for (PlayerListEntry entry : network.getPlayerList().stream().sorted(ORDER).toList()) {
                var displayName = entry.getDisplayName();
                if (displayName == null) continue;
                var content = displayName.getString();
                var profileName = entry.getProfile().getName();
                if (profileName.endsWith("a")) { // Column headers
                    if (content.trim().equals("Info")) {
                        inInfoColumn = true;
                        continue;
                    }
                }
                if (!inInfoColumn) continue;
                if (content.isBlank()) continue;
                if (content.startsWith(" ")) {
                    if (currentWidget != null) {
                        widgetLines.get(currentWidget).add(content);
                    }

                } else {
                    var widget = Widget.byPrefix(content.split(":")[0]);
                    if (widget != null) {
                        currentWidget = widget;
                        widgetLines.putIfAbsent(currentWidget, new ArrayList<>());
                        widgetLines.get(currentWidget).add(content);
                        continue;
                    } else {
                        currentWidget = null;
                    }

                }
            }
            activeWidgets = widgetLines.keySet();
        } catch (Exception e) {
            Skyblock21.LOGGER.warn("Failed to parse tab list", e);
        }
    }

}
