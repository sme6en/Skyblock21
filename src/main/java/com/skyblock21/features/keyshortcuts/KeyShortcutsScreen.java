package com.skyblock21.features.keyshortcuts;

import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.gui.Theme;
import com.skyblock21.gui.ThemeManager;
import com.skyblock21.util.ColorUtil;
import com.skyblock21.util.TickSchedulerHelper;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import com.skyblock21.gui.components.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static net.minecraft.text.Text.literal;

public class KeyShortcutsScreen extends BaseOwoScreen<FlowLayout> {

    private final List<ShortcutEntry> entries = new
            ArrayList<>();
    private ScrollContainer<FlowLayout> scrollContainer;
    private FlowLayout entriesContainer;
    private ShortcutEntry listeningEntry = null;

    private Animation animation;

    private Screen parent;

    public KeyShortcutsScreen(Screen parent) {
        this.parent = parent;
        loadEntries();
    }

    private void loadEntries() {
        entries.clear();

        for (Shortcut shortcut : PersistentData.get().shortcuts) {
            entries.add(new ShortcutEntry(shortcut));
        }

        if (entries.isEmpty() || !entries.get(entries.size() - 1).isEmpty()) {
            entries.add(new ShortcutEntry(new Shortcut()));
        }
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        Theme theme = ThemeManager.getCurrentTheme();

        rootComponent.horizontalAlignment(HorizontalAlignment.CENTER)
                     .verticalAlignment(VerticalAlignment.CENTER)
                     .surface(Surface.blur(3, 10));

        // Main container
        FlowLayout mainContainer = (FlowLayout) Containers.verticalFlow(Sizing.fill(85), Sizing.fixed(0)).horizontalAlignment(HorizontalAlignment.CENTER);

        animation = mainContainer.verticalSizing().animate(500, Easing.CUBIC, Sizing.fill(90));


        mainContainer.gap(10);
        rootComponent.child(mainContainer);

        mainContainer.child(new Label(literal("Key Shortcuts"))
                .shadow(true).color(Color.ofArgb(ColorUtil.getIntFromColor(theme.getPrimary()))));

        // Header row
        FlowLayout headerRow = (FlowLayout) Containers.horizontalFlow(Sizing.fill(), Sizing.content()).padding(Insets.both(5, 5));
        headerRow.gap(10);
        headerRow.child(new Label(literal("Enabled")).color(Color.ofArgb(ColorUtil.getIntFromColor(theme.text))).horizontalSizing(Sizing.fill(6)))
                 .child(new Label(literal("Command")).color(Color.ofArgb(ColorUtil.getIntFromColor(theme.text))).horizontalSizing(Sizing.fill(45)))
                 .child(new Label(literal("Keybind")).color(Color.ofArgb(ColorUtil.getIntFromColor(theme.text))).horizontalSizing(Sizing.fill(20)))
                 .child(new Label(literal("Actions")).color(Color.ofArgb(ColorUtil.getIntFromColor(theme.text))).horizontalSizing(Sizing.fill(7)));

        mainContainer.child(new RoundedContainer(Sizing.content(), Sizing.content(), theme.getRounding(), theme.getSecondaryBackground(), FlowLayout.Algorithm.HORIZONTAL)
                .child(headerRow)
                .padding(Insets.both(10, 5)));

        entriesContainer = Containers.verticalFlow(Sizing.fill(), Sizing.content());
        entriesContainer.gap(5);

        scrollContainer = Containers.verticalScroll(Sizing.fill(), Sizing.fill(80), entriesContainer);
        scrollContainer.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(ColorUtil.getIntFromColor(theme.getPrimary()))));

        mainContainer.child(new RoundedContainer(Sizing.content(), Sizing.content(), theme.getRounding(), theme.background, FlowLayout.Algorithm.VERTICAL)
                .child(scrollContainer)
                .surface(Surface.VANILLA_TRANSLUCENT)
                .padding(Insets.both(10, 10)));

        // Bottom buttons
        FlowLayout buttonsContainer = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        buttonsContainer.gap(10);
        buttonsContainer.child(new Button(literal("Cancel"), theme.getSecondaryBackground(), theme.text, button -> {
                            super.close();
                        }).textShadow(false).horizontalSizing(Sizing.content(6)));

        mainContainer.child(buttonsContainer);

        rebuildEntries();
    }

    @Override
    public void init() {
        super.init();
        animation.forwards();
    }

    private void rebuildEntries() {
        saveFormState();
        entriesContainer.clearChildren();

        for (int i = 0; i < entries.size(); i++) {
            final int index = i;
            ShortcutEntry entry = entries.get(i);

            // Create entry row
            FlowLayout entryRow = Containers.horizontalFlow(Sizing.content(), Sizing.content());
            entryRow.gap(10);
            entryRow.margins(Insets.vertical(2));
            entryRow.verticalAlignment(VerticalAlignment.CENTER);
            // Enabled checkbox
            entry.enabledBox = (Checkbox) new Checkbox(literal(""))
                    .checked(entry.shortcut.enabled);
//            entry.enabledBox.margins(Insets.right(18).withLeft(5));
            entryRow.child(entry.enabledBox.sizing(Sizing.fixed(16)));

            // Command field
            entry.commandField = new TextBox(Sizing.fill(50), literal("Command"));
            entry.commandField.setText(entry.shortcut.command);
            entry.commandField.setMaxLength(500);

            entryRow.child(entry.commandField);

            // Keybind button
            entry.keybindButton = new KeybindButton(entry, () -> {
                saveFormState();
                if (index == entries.size() - 1 && (!entry.shortcut.command.trim().isEmpty() || entry.shortcut.keyCode != -1)) {
                    entries.add(new ShortcutEntry(new Shortcut()));
                    rebuildEntries();
                }
            });
//            entry.keybindButton.margins(Insets.right(130));
            entryRow.child(entry.keybindButton.horizontalSizing(Sizing.fill(20)));

            // Remove button
            Button removeButton = (Button) new Button(literal("Remove"),
                    ThemeManager.getCurrentTheme().getSecondaryBackground(),
                    ThemeManager.getCurrentTheme().text,
                    button -> {
                        saveFormState();
                        entries.remove(index);
                        rebuildEntries();
                    })
                    .textShadow(false)
                    .horizontalSizing(Sizing.fill(20));
            entryRow.child(removeButton);

            // Check for keybind conflicts
            if (entry.shortcut.keyCode != -1 && listeningEntry != entry) {
                boolean hasConflict = entries.stream()
                                             .anyMatch(other -> other != entry &&
                                                     other.shortcut.keyCode != -1 &&
                                                     other.shortcut.keyCode == entry.shortcut.keyCode &&
                                                     other.shortcut.modifiers.equals(entry.shortcut.modifiers));

                if (hasConflict) {
                    entryRow.child(new Label(literal("X")).color(Color.RED));
                }
            }

            Theme theme = ThemeManager.getCurrentTheme();
            FlowLayout entryContainer = (FlowLayout) new RoundedContainer(Sizing.content(), Sizing.content(),
                    theme.getRounding(), theme.getBackground(), FlowLayout.Algorithm.HORIZONTAL)
                    .child(entryRow)
                    .padding(Insets.both(8, 5));

            entriesContainer.child(entryContainer);
        }

        // Show entry count
        Label countLabel = (Label) new Label(literal(String.format("Total entries: %d", entries.size())))
                .color(Color.ofArgb(ColorUtil.getIntFromColor(ThemeManager.getCurrentTheme().text)));
        entriesContainer.child(countLabel);
    }

    private void saveFormState() {
        for (ShortcutEntry entry : entries) {
            if (entry.commandField != null) {
                entry.shortcut.command = entry.commandField.getText();
            }
            if (entry.enabledBox != null) {
                entry.shortcut.enabled = entry.enabledBox.isChecked();
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listeningEntry != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                listeningEntry.shortcut.keyCode = -1;
                listeningEntry.shortcut.modifiers = new HashSet<>();
            } else if (isModifierKey(keyCode)) {
                return true;
            } else {
                listeningEntry.shortcut.keyCode = keyCode;
                listeningEntry.shortcut.modifiers = new HashSet<>();

                if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                    listeningEntry.shortcut.modifiers.add(Modifier.CTRL);
                }
                if ((modifiers & GLFW.GLFW_MOD_ALT) != 0) {
                    listeningEntry.shortcut.modifiers.add(Modifier.ALT);
                }
                if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) {
                    listeningEntry.shortcut.modifiers.add(Modifier.SHIFT);
                }
            }

            listeningEntry.keybindButton.notifyKeybindSet();
            listeningEntry = null;
            rebuildEntries();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (listeningEntry != null) {
            listeningEntry.shortcut.keyCode = button + 1000;
            listeningEntry.shortcut.modifiers = new HashSet<>();

            long window = MinecraftClient.getInstance().getWindow().getHandle();
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS ||
                    GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS) {
                listeningEntry.shortcut.modifiers.add(Modifier.CTRL);
            }
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS ||
                    GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS) {
                listeningEntry.shortcut.modifiers.add(Modifier.ALT);
            }
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                    GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS) {
                listeningEntry.shortcut.modifiers.add(Modifier.SHIFT);
            }

            listeningEntry = null;
            rebuildEntries();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isModifierKey(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_LEFT_CONTROL ||
                keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL ||
                keyCode == GLFW.GLFW_KEY_LEFT_ALT ||
                keyCode == GLFW.GLFW_KEY_RIGHT_ALT ||
                keyCode == GLFW.GLFW_KEY_LEFT_SHIFT ||
                keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT ||
                keyCode == GLFW.GLFW_KEY_LEFT_SUPER ||
                keyCode == GLFW.GLFW_KEY_RIGHT_SUPER;
    }

    private void saveAndClose() {
        saveFormState();
        PersistentData.get().shortcuts.clear();

        for (ShortcutEntry entry : entries) {
            if (entry.commandField != null) {
                entry.shortcut.command = entry.commandField.getText();
            }
            if (entry.enabledBox != null) {
                entry.shortcut.enabled = entry.enabledBox.isChecked();
            }

            if (!entry.shortcut.command.trim().isEmpty() && entry.shortcut.keyCode != -1) {
                PersistentData.get().shortcuts.add(entry.shortcut);
            }
        }

        PersistentData.save();
        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    public void close() {
        animation.backwards();
        TickSchedulerHelper.runAfter(this::saveAndClose, 10);
    }

    private static class ShortcutEntry {
        public Shortcut shortcut;
        public TextBox commandField;
        public Checkbox enabledBox;
        public KeybindButton keybindButton;

        public ShortcutEntry(Shortcut shortcut) {
            this.shortcut = shortcut;
        }

        public boolean isEmpty() {
            return shortcut.command.trim().isEmpty() && shortcut.keyCode == 0;
        }
    }

    private class KeybindButton extends Button {
        private ShortcutEntry entry;

        private Runnable onKeybindSet;

        public KeybindButton(ShortcutEntry entry, Runnable onKeybindSet) {
            super(getButtonText(entry),
                    ThemeManager.getCurrentTheme().getSecondaryBackground(),
                    ThemeManager.getCurrentTheme().text,
                    button -> {
                        listeningEntry = entry;
                        rebuildEntries();
                    });
            this.entry = entry;
            this.onKeybindSet = onKeybindSet;
            this.textShadow(false);
        }

        private static Text getButtonText(ShortcutEntry entry) {
            if (entry.shortcut.keyCode == -1) {
                return literal("Click to set");
            }
            return literal(entry.shortcut.getDisplayString());
        }

        @Override
        protected Text getText() {
            if (listeningEntry == entry) {
                return literal("Press a key...").formatted(Formatting.YELLOW);
            }
            return getButtonText(entry);
        }

        @Override
        protected java.awt.Color getBgColor() {

            return listeningEntry == entry ? ThemeManager.getCurrentTheme().getPrimaryActive() : ThemeManager.getCurrentTheme().getSecondary();
        }

        @Override
        protected java.awt.Color getTextColor() {
            if (entry.shortcut.keyCode == -1) {
                return ThemeManager.getCurrentTheme().getTextSecondary();
            }

            return ThemeManager.getCurrentTheme().text;
        }

        public void notifyKeybindSet() {
            if (onKeybindSet != null) {
                onKeybindSet.run();
            }
        }
    }
}
