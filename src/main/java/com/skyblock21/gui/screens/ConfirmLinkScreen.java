package com.skyblock21.gui.screens;

import com.skyblock21.gui.Theme;
import com.skyblock21.gui.ThemeManager;
import com.skyblock21.gui.components.Button;
import com.skyblock21.gui.components.Label;
import com.skyblock21.gui.components.RoundedContainer;
import com.skyblock21.util.ColorUtil;
import com.skyblock21.util.TickSchedulerHelper;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static net.minecraft.text.Text.literal;

public class ConfirmLinkScreen extends BaseOwoScreen<FlowLayout> {

    private static Animation animation;
    private final Consumer<Boolean> callback;
    private final String url;
    private final boolean trusted;

    public ConfirmLinkScreen(Consumer<Boolean> callback, String url, boolean trusted) {
        this.callback = callback;
        this.url = url;
        this.trusted = trusted;

    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    public void init() {
        super.init();
        if (animation != null) {
            animation.forwards();
        }
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        Theme theme = ThemeManager.getCurrentTheme();

        rootComponent.surface(Surface.blur(3, 10))
                     .horizontalAlignment(HorizontalAlignment.CENTER)
                     .verticalAlignment(VerticalAlignment.CENTER);

        FlowLayout mainContainer = (FlowLayout) Containers.verticalFlow(Sizing.content(), Sizing.content())
                                                          .horizontalAlignment(HorizontalAlignment.CENTER)
                                                          .verticalAlignment(VerticalAlignment.CENTER);

        rootComponent.child(mainContainer);

        // Main content container with animation
        RoundedContainer contentContainer = new RoundedContainer(
                Sizing.content(),
                Sizing.fixed(20),
                theme.getRounding(),
                theme.getBackground(),
                FlowLayout.Algorithm.VERTICAL
        );

        animation = contentContainer.verticalSizing().animate(500, Easing.CUBIC, Sizing.content());
        contentContainer.surface(Surface.VANILLA_TRANSLUCENT)
                        .padding(Insets.of(25)).horizontalAlignment(HorizontalAlignment.CENTER);
        contentContainer.gap(12);

        // Title
        String titleText = trusted ? "Open Link?" : "Open Untrusted Link?";
        Label title = (Label) new Label(literal(titleText))
                .color(Color.ofArgb(ColorUtil.getIntFromColor(theme.getSecondary())))
                .shadow(true);
        title.horizontalTextAlignment(HorizontalAlignment.CENTER);
        mainContainer.child(title);
        mainContainer.gap(15);

        // Warning message
        String warningText = trusted ?
                "This will open the following link in your default browser:" :
                "This will open an untrusted link in your default browser:";

        Label warningLabel = (Label) new Label(literal(warningText))
                .color(Color.ofArgb(ColorUtil.getIntFromColor(theme.getTextSecondary())))
                .shadow(false);
        warningLabel.horizontalTextAlignment(HorizontalAlignment.CENTER);
        contentContainer.child(warningLabel);

        // URL display
        String displayUrl = url.length() > 50 ? url.substring(0, 47) + "..." : url;
        Label urlLabel = (Label) new Label(literal(displayUrl))
                .color(Color.ofArgb(ColorUtil.getIntFromColor(theme.getPrimary())))
                .shadow(false);
        urlLabel.horizontalTextAlignment(HorizontalAlignment.CENTER);
        contentContainer.child(urlLabel);

        // Additional warning for untrusted links
        if (!trusted) {
            Label untrustedWarning = (Label) new Label(literal("§c§lWarning: This link may not be safe!"))
                    .color(Color.ofArgb(ColorUtil.getIntFromColor(java.awt.Color.RED)))
                    .shadow(false);
            untrustedWarning.horizontalTextAlignment(HorizontalAlignment.CENTER);
            contentContainer.child(untrustedWarning);
        }

        // Spacer
        contentContainer.child(Components.spacer().sizing(Sizing.fixed(8)));

        // Buttons container
        FlowLayout buttonsContainer = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        buttonsContainer.gap(10);

        // Open button
        Button openButton = (Button) new Button(
                literal(trusted ? "Open Link" : "Open Anyway"),
                trusted ? theme.getPrimary() : java.awt.Color.ORANGE,
                theme.getBackground(),
                button -> confirmLink(true)
        ).textShadow(false).horizontalSizing(Sizing.fixed(100));

        // Cancel button
        Button cancelButton = (Button) new Button(
                literal("Cancel"),
                theme.getSecondaryBackground(),
                theme.text,
                button -> confirmLink(false)
        ).textShadow(false).horizontalSizing(Sizing.fixed(100));

        Button copyButton = (Button) new Button(
                literal("Copy URL"),
                theme.getSecondaryBackground(),
                theme.getTextSecondary(),
                button -> copyUrlToClipboard()
        ).textShadow(false).horizontalSizing(Sizing.fixed(90));

        buttonsContainer.child(openButton).child(cancelButton).child(copyButton);
        contentContainer.child(buttonsContainer);


        mainContainer.child(contentContainer);
    }

    private void confirmLink(boolean confirmed) {
        if (animation != null) {
            animation.backwards();
            TickSchedulerHelper.runAfter(() -> {
                if (callback != null) {
                    callback.accept(confirmed);
                }
            }, 10);
        } else {
            if (callback != null) {
                callback.accept(confirmed);
            }
        }
    }

    private void copyUrlToClipboard() {
        if (client != null) {
            client.keyboard.setClipboard(url);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            confirmLink(false);
            return true;
        } else if (keyCode == 32) { // Space
            confirmLink(true);
            return true;
        } else if (keyCode == 257) { // Enter
            confirmLink(true);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        confirmLink(false);
    }

    @Override
    public boolean shouldPause() {
        return true;
    }
}