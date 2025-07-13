package com.skyblock21.hud.elements;

import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.features.Scathas;
import com.skyblock21.hud.HudElement;
import com.skyblock21.hud.MultiLineHudElement;
import com.skyblock21.util.Location;
import com.skyblock21.util.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

import static net.minecraft.text.Text.literal;

public class ScathaTrackerElement extends MultiLineHudElement {

    public ScathaTrackerElement(int x, int y) {
        super(x, y);

        addAmountLine("worms", "§bWorms", 0);
        addLine("scathas", literal("§bScathas: §f0 §7(0%)"));
        addAmountLine("spawns_since_drop", "§7Spawns since pet drop", 0);
        addLine("last_spawn", literal("§7Last Scatha spawn: §f28s ago"));
        addLine("cannot_spawn", literal("§cCannot spawn scatha yet! 5s"));
    }

    @Override
    protected void renderElement(DrawContext context) {
        MatrixStack matrices = context.getMatrices();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        matrices.push();
        matrices.scale(1.1f, 1.1f, 1.0f);
        context.drawTextWithShadow(textRenderer, "§lScatha Tracker", 2, 2, new Color(134, 239, 244).getRGB());
        matrices.pop();

        super.renderElement(context);
    }

    @Override
    protected void onTick(MinecraftClient client) {
        Scathas.ScathasData data = PersistentData.get().scathasData;
        if (data == null) return;

        updateAmountLine("worms", data.wormsSpawned);
        updateLine("scathas", literal(String.format("§bScathas: §f%d §7(%.0f%%)", data.scathasSpawned, data.scathasSpawned > 0 ? (data.scathasSpawned * 100.0 / (data.wormsSpawned + data.scathasSpawned)) : 0.0)));
        updateAmountLine("spawns_since_drop", data.sinceLastScathaPetDropSpawns);
        updateLine("last_spawn", literal(String.format("§7Last Scatha spawn: §f%s ago", data.lastSpawnTime == -1 || System.currentTimeMillis() - data.lastSpawnTime >= 24 * 60 * 60 * 1000 ? "N/A" : Utils.formatTime(System.currentTimeMillis() - data.lastSpawnTime))));
        updateLine("cannot_spawn", literal(String.format("%s", System.currentTimeMillis() - data.lastScathaKillTime < 30000 ? "§cCannot spawn Scatha yet! " + Utils.formatTime(30000 - (System.currentTimeMillis() - data.lastScathaKillTime)) : "")));
    }

    @Override
    protected void renderDummy(DrawContext context) {
        MatrixStack matrices = context.getMatrices();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        matrices.push();
        matrices.scale(1.1f, 1.1f, 1.0f);
        context.drawTextWithShadow(textRenderer, "§lScatha Tracker", 2, 2, new Color(134, 239, 244).getRGB());
        matrices.pop();

        super.renderElement(context);
    }

    @Override
    public boolean isEnabled() {
        return Skyblock21ConfigManager.get().mining.scathaTracker;
    }

    @Override
    public boolean isAllowedInLocation(Location location) {
        return !Skyblock21ConfigManager.get().mining.showOnlyInCrystalHollows || location.equals(Location.CRYSTAL_HOLLOWS);
    }
}
