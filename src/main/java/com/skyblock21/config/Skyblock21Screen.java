package com.skyblock21.config;

import com.skyblock21.Skyblock21;
import com.skyblock21.features.commandaliases.CommandAliasesScreen;
import com.skyblock21.features.keyshortcuts.KeyShortcutsScreen;
import com.skyblock21.hud.EditGuiScreenV2;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.screen.ScreenTexts;

import static net.minecraft.text.Text.literal;

public class Skyblock21Screen extends Screen {

    private static final int SPACING = 8;

    private ThreePartsLayoutWidget layout;

    public Skyblock21Screen() {
        super(literal("Skyblock21 " + Skyblock21.MOD_VERSION));
    }

    @Override
    public void init() {

        this.layout = new ThreePartsLayoutWidget(this, 50, 100);
        this.layout.addHeader(new TextWidget(this.getTitle(), this.textRenderer));

        GridWidget gridWidget = this.layout.addBody(new GridWidget()).setSpacing(SPACING);
        gridWidget.getMainPositioner().alignHorizontalCenter();
        GridWidget.Adder adder = gridWidget.createAdder(2);

        adder.add(ButtonWidget.builder(literal("Config"), button -> this.openConfig()).width(210).build(), 2);
        adder.add(ButtonWidget.builder(literal("Edit Gui"), button -> this.openGuiEditor()).width(210).build(), 2);
        adder.add(ButtonWidget.builder(literal("Keybind shortcuts"), button -> this.openKeybinds()).width(210).build(), 2);
        adder.add(ButtonWidget.builder(literal("Command aliases"), button -> this.openAliases()).width(210).build(), 2);
        adder.add(ButtonWidget.builder(literal("Discord server"), ConfirmLinkScreen.opening(this, "https://discord.gg/NMNSwQH6dr")).width(101).build());
        adder.add(ButtonWidget.builder(literal("Report bugs"), ConfirmLinkScreen.opening(this, "https://github.com/sme6en/Skyblock21/issues")).width(101).build());
        adder.add(ButtonWidget.builder(literal("Modrinth"), ConfirmLinkScreen.opening(this, "https://modrinth.com/mod/skyblock21")).width(101).build());
        adder.add(ButtonWidget.builder(literal("Source code"), ConfirmLinkScreen.opening(this, "https://github.com/sme6en/Skyblock21")).width(101).build());
        adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).width(210).build(), 2);

        this.layout.refreshPositions();
        this.layout.forEachChild(this::addDrawableChild);
    }

    @Override
    protected void refreshWidgetPositions() {
        super.refreshWidgetPositions();
        this.layout.refreshPositions();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    private void openConfig() {
        this.client.setScreen(Skyblock21ConfigManager.createGUI(this));
    }

    private void openGuiEditor() {
        this.client.setScreen(new EditGuiScreenV2(this.client.currentScreen));
    }

    private void openKeybinds() {
        this.client.setScreen(new KeyShortcutsScreen());
    }

    private void openAliases() {
        this.client.setScreen(new CommandAliasesScreen(this.client.currentScreen));
    }
}
