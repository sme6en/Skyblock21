package com.skyblock21.config;

import com.skyblock21.Skyblock21;
import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.features.commandaliases.CommandAliasesScreen;
import com.skyblock21.features.keyshortcuts.KeyShortcutsScreen;
import com.skyblock21.gui.Theme;
import com.skyblock21.gui.ThemeManager;
import com.skyblock21.gui.components.Button;
import com.skyblock21.gui.components.Label;
import com.skyblock21.gui.components.RoundedContainer;
import com.skyblock21.gui.screens.ConfirmLinkScreen;
import com.skyblock21.hud.EditGuiScreen;
import com.skyblock21.util.ColorUtil;
import com.skyblock21.util.TickSchedulerHelper;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import net.minecraft.util.Identifier;

import static net.minecraft.text.Text.literal;

public class Skyblock21Screen extends BaseOwoScreen<FlowLayout> {
    private static Animation animation;

    public Skyblock21Screen() {
    }

    private void switchTheme() {
        // Get current theme and switch to next one
        Theme currentTheme = ThemeManager.getCurrentTheme();
        Theme[] themes = Theme.values();

        // Find current theme index
        int currentIndex = 0;
        for (int i = 0; i < themes.length; i++) {
            if (themes[i] == currentTheme) {
                currentIndex = i;
                break;
            }
        }

        // Switch to next theme (loop back to first if at end)
        int nextIndex = (currentIndex + 1) % themes.length;
        ThemeManager.setTheme(themes[nextIndex]);
        PersistentData.get().theme = themes[nextIndex];

        animation.backwards();
        TickSchedulerHelper.runAfter(() -> {
            // Rebuild the screen with new theme
            uiAdapter.rootComponent.clearChildren();
            build(uiAdapter.rootComponent);
            animation.forwards();
        }, 10);
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    public void init() {
        super.init();
        animation.forwards();
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        var theme = ThemeManager.getCurrentTheme();

        rootComponent.surface(Surface.blur(3, 10))
                     .horizontalAlignment(HorizontalAlignment.CENTER)
                     .verticalAlignment(VerticalAlignment.CENTER);

        FlowLayout mainContainer = (FlowLayout) Containers.verticalFlow(Sizing.fill(85), Sizing.fill(90))
                                                          .horizontalAlignment(HorizontalAlignment.CENTER)
                                                            .verticalAlignment(VerticalAlignment.CENTER);

        TextureComponent logo = Components.texture(Identifier.of(Skyblock21.MOD_ID, "skyblock21_logo.png"), 0, 0, 300, 90, 300, 90);
        mainContainer.gap(10);
        rootComponent.child(mainContainer);

        var actionsContainer = new RoundedContainer(Sizing.content(), Sizing.fixed(20), theme.getRounding(), theme.getBackground(), FlowLayout.Algorithm.VERTICAL);
        animation = actionsContainer.verticalSizing().animate(500, Easing.CUBIC, Sizing.content());

        actionsContainer.surface(Surface.VANILLA_TRANSLUCENT).padding(Insets.of(20));

        actionsContainer.gap(8);

        actionsContainer.child(logo);

        actionsContainer.child(new Button(literal("Config"), theme.getPrimary(), theme.getBackground(), button -> {
                            openConfig();
                        }).textShadow(false).horizontalSizing(Sizing.fixed(210)))
                        .child(new Button(literal("Edit Gui"), theme.getPrimary(), theme.getBackground(), button -> {
                            openGuiEditor();
                        }).textShadow(false).horizontalSizing(Sizing.fixed(210)))
                        .child(new Button(literal("Keybind shortcuts"), theme.getPrimary(), theme.getBackground(), button -> {
                            openKeybinds();
                        }).textShadow(false).horizontalSizing(Sizing.fixed(210)))
                        .child(new Button(literal("Command aliases"), theme.getPrimary(), theme.getBackground(), button -> {
                            openAliases();
                        }).textShadow(false).horizontalSizing(Sizing.fixed(210)))
                        .child(new Button(literal("Theme: " + theme.name()), theme.getSecondaryBackground(), theme.text, button -> {
                            switchTheme();
                        }).textShadow(false).horizontalSizing(Sizing.fixed(210)));

        actionsContainer.child(new Label(literal("Links")).color(Color.ofArgb(ColorUtil.getIntFromColor(theme.getPrimary()))).shadow(false)).horizontalAlignment(HorizontalAlignment.CENTER);

        // Links section - two columns
        var linksContainer = Containers.verticalFlow(Sizing.fixed(210), Sizing.content());
        linksContainer.gap(8);

        // First row of links
        var linksRow1 = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        linksRow1.gap(8);
        linksRow1.child(new Button(literal("Discord server"), theme.getSecondaryBackground(), theme.text, button -> {
                     openLink("https://discord.gg/NMNSwQH6dr");
                 }).textShadow(false).horizontalSizing(Sizing.fixed(101)))
                 .child(new Button(literal("Report bugs"), theme.getSecondaryBackground(), theme.text, button -> {
                     openLink("https://github.com/sme6en/Skyblock21/issues");
                 }).textShadow(false).horizontalSizing(Sizing.fixed(101)));

        // Second row of links
        var linksRow2 = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        linksRow2.gap(8);
        linksRow2.child(new Button(literal("Modrinth"), theme.getSecondaryBackground(), theme.text, button -> {
                     openLink("https://modrinth.com/mod/skyblock21");
                 }).textShadow(false).horizontalSizing(Sizing.fixed(101)))
                 .child(new Button(literal("Source code"), theme.getSecondaryBackground(), theme.text, button -> {
                     openLink("https://github.com/sme6en/Skyblock21");
                 }).textShadow(false).horizontalSizing(Sizing.fixed(101)));

        linksContainer.child(linksRow1).child(linksRow2);

        actionsContainer.child(linksContainer);

        actionsContainer.child(Components.spacer().sizing(Sizing.fixed(10)));
        actionsContainer.child(new Button(literal("Done"), theme.getPrimary(), theme.getBackground(), button -> {
            close();
        }).textShadow(false).horizontalSizing(Sizing.fixed(210)));

        mainContainer.child(actionsContainer);
    }

    private void openConfig() {
        if (client != null) {
            client.setScreen(Skyblock21ConfigManager.createGUI(this));
        }
    }

    private void openGuiEditor() {
        animation.backwards();
        TickSchedulerHelper.runAfter(() -> {
            if (client != null) {
                client.setScreen(new EditGuiScreen(this));
            }
        }, 10);
    }

    private void openKeybinds() {
        animation.backwards();
        TickSchedulerHelper.runAfter(() -> {
            if (client != null) {
                client.setScreen(new KeyShortcutsScreen(this));
            }
        }, 10);
    }

    private void openAliases() {
        animation.backwards();
        TickSchedulerHelper.runAfter(() -> {
            if (client != null) {
                client.setScreen(new CommandAliasesScreen(this));
            }
        }, 10);
    }

    private void openLink(String url) {
        if (client != null) {
            client.setScreen(new ConfirmLinkScreen(confirmed -> {
                if (confirmed) {
                    Util.getOperatingSystem().open(url);
                }
                client.setScreen(this);
            }, url, true));
        }
    }

    @Override
    public void close() {
        animation.backwards();
        TickSchedulerHelper.runAfter(super::close, 10);
    }
}