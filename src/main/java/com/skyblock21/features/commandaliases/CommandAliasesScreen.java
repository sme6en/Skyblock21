package com.skyblock21.features.commandaliases;

import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.gui.Theme;
import com.skyblock21.gui.ThemeManager;
import com.skyblock21.gui.components.*;
import com.skyblock21.util.ColorUtil;
import com.skyblock21.util.TickSchedulerHelper;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.text.Text.literal;

public class CommandAliasesScreen extends BaseOwoScreen<FlowLayout> {
    private final Screen parent;
    private final List<AliasEntry> entries = new ArrayList<>();
    private FlowLayout entriesContainer;
    private ScrollContainer<FlowLayout> scrollContainer;
    private static Animation animation;

    public CommandAliasesScreen(Screen parent) {
        this.parent = parent;
        loadAliases();
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
        Theme theme = ThemeManager.getCurrentTheme();

        rootComponent
                .surface(Surface.blur(3, 10))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);

        // Main container with animation
        FlowLayout mainContainer = (FlowLayout) Containers.verticalFlow(Sizing.fill(85), Sizing.fixed(0))
                                                          .horizontalAlignment(HorizontalAlignment.CENTER);

        animation = mainContainer.verticalSizing().animate(500, Easing.CUBIC, Sizing.fill(90));
        mainContainer.gap(10);
        rootComponent.child(mainContainer);

        // Title
        mainContainer.child(new Label(literal("Command Aliases"))
                .shadow(true)
                .color(Color.ofArgb(ColorUtil.getIntFromColor(theme.getPrimary()))));

        // Header row with column titles
        FlowLayout headerRow = (FlowLayout) Containers.horizontalFlow(Sizing.fill(), Sizing.content())
                                                      .padding(Insets.both(5, 5));
        headerRow.gap(10);
        headerRow.child(new Label(literal("Enabled"))
                         .color(Color.ofArgb(ColorUtil.getIntFromColor(theme.text)))
                         .horizontalSizing(Sizing.fill(7)))
                 .child(new Label(literal("Alias Command"))
                         .color(Color.ofArgb(ColorUtil.getIntFromColor(theme.text)))
                         .horizontalSizing(Sizing.fill(30)))
                 .child(new Label(literal("Target Command"))
                         .color(Color.ofArgb(ColorUtil.getIntFromColor(theme.text)))
                         .horizontalSizing(Sizing.fill(50)))
                 .child(new Label(literal("Actions"))
                         .color(Color.ofArgb(ColorUtil.getIntFromColor(theme.text)))
                         .horizontalSizing(Sizing.fill(20)));

        mainContainer.child(new RoundedContainer(
                Sizing.content(),
                Sizing.content(),
                theme.getRounding(),
                theme.getSecondaryBackground(),
                FlowLayout.Algorithm.HORIZONTAL)
                .child(headerRow)
                .padding(Insets.both(10, 5)));

        // Entries container with scrolling (main body)
        entriesContainer = Containers.verticalFlow(Sizing.fill(), Sizing.content());
        entriesContainer.gap(5);

        scrollContainer = Containers.verticalScroll(Sizing.fill(), Sizing.fill(80), entriesContainer);
        scrollContainer.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(ColorUtil.getIntFromColor(theme.getPrimary()))));

        mainContainer.child(new RoundedContainer(
                Sizing.content(),
                Sizing.content(),
                theme.getRounding(),
                theme.getBackground(),
                FlowLayout.Algorithm.VERTICAL)
                .child(scrollContainer)
                .surface(Surface.VANILLA_TRANSLUCENT)
                .padding(Insets.both(10, 10)));

        // Bottom buttons
        FlowLayout buttonsContainer = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        buttonsContainer.gap(10);
        buttonsContainer.child(new Button(literal("Add New Alias"), theme.getPrimary(), theme.getBackground(), button -> {
                            addNewEntry();
                        }).textShadow(false)
                          .horizontalSizing(Sizing.content(10)))
                        .child(new Button(literal("Done"), theme.getPrimary(), theme.getBackground(), button -> {
                            save();
                            close();
                        }).textShadow(false)
                          .horizontalSizing(Sizing.content(6)))
                        .child(new Button(literal("Cancel"), theme.getSecondaryBackground(), theme.text, button -> {
                            close();
                        }).textShadow(false)
                          .horizontalSizing(Sizing.content(6)));

        mainContainer.child(buttonsContainer);

        buildEntryWidgets();
    }

    private void loadAliases() {
        entries.clear();
        for (Alias alias : PersistentData.get().aliases) {
            entries.add(new AliasEntry(alias));
        }
        // Always add an empty entry at the end
        if (entries.isEmpty() || !entries.get(entries.size() - 1).isEmpty()) {
            entries.add(new AliasEntry(new Alias()));
        }
    }

    private void buildEntryWidgets() {
        if (entriesContainer == null) return;

        saveFormState();
        entriesContainer.clearChildren();

        for (int i = 0; i < entries.size(); i++) {
            final int index = i;
            AliasEntry entry = entries.get(i);

            // Create entry row
            FlowLayout entryRow = Containers.horizontalFlow(Sizing.content(), Sizing.content());
            entryRow.gap(10);
            entryRow.margins(Insets.vertical(2));
            entryRow.verticalAlignment(VerticalAlignment.CENTER);

            // Enabled checkbox
            entry.enabledCheckbox = (Checkbox) new Checkbox(literal(""))
                    .checked(entry.alias.enabled);
//            entry.enabledCheckbox.margins(Insets.right(20).withLeft(5));
            entryRow.child(entry.enabledCheckbox.sizing(Sizing.fixed(16)));

            // Alias command field
            entry.aliasField = new TextBox(Sizing.fill(30), literal("Alias"));
            entry.aliasField.setText(entry.alias.aliasCommand);
            entry.aliasField.setMaxLength(64);
            entryRow.child(entry.aliasField);

            // Target command field
            entry.targetField = new TextBox(Sizing.fill(50), literal("Target"));
            entry.targetField.setText(entry.alias.targetCommand);
            entry.targetField.setMaxLength(512);
            entryRow.child(entry.targetField);

            // Remove button
            Button removeButton = (Button) new Button(literal("Remove"),
                    ThemeManager.getCurrentTheme().getSecondaryBackground(),
                    ThemeManager.getCurrentTheme().text,
                    button -> {
                        removeEntry(index);
                    })
                    .textShadow(false)
                    .horizontalSizing(Sizing.fill(7));
            entryRow.child(removeButton);

            Theme theme = ThemeManager.getCurrentTheme();
            FlowLayout entryContainer = (FlowLayout) new RoundedContainer(
                    Sizing.content(),
                    Sizing.content(),
                    theme.getRounding(),
                    theme.getBackground(),
                    FlowLayout.Algorithm.HORIZONTAL)
                    .child(entryRow)
                    .padding(Insets.both(8, 5));

            entriesContainer.child(entryContainer);
        }

        // Show entry count
        Label countLabel = (Label) new Label(literal(String.format("Aliases: %d", Math.max(0, entries.size() - 1))))
                .color(Color.ofArgb(ColorUtil.getIntFromColor(ThemeManager.getCurrentTheme().getTextSecondary())));
        entriesContainer.child(countLabel);
    }

    private void addNewEntry() {
        saveFormState();
        entries.add(new AliasEntry(new Alias()));
        buildEntryWidgets();
    }

    private void removeEntry(int index) {
        updateEntriesFromFields();
        if (index < entries.size()) {
            entries.remove(index);
            buildEntryWidgets();
        }
    }

    private void saveFormState() {
        for (AliasEntry entry : entries) {
            if (entry.aliasField != null) {
                entry.alias.aliasCommand = entry.aliasField.getText();
            }
            if (entry.targetField != null) {
                entry.alias.targetCommand = entry.targetField.getText();
            }
            if (entry.enabledCheckbox != null) {
                entry.alias.enabled = entry.enabledCheckbox.isChecked();
            }
        }
    }

    private void updateEntriesFromFields() {
        saveFormState();

        if (!entries.isEmpty()) {
            AliasEntry lastEntry = entries.get(entries.size() - 1);
            if (!lastEntry.isEmpty() && (!lastEntry.alias.aliasCommand.trim().isEmpty() || !lastEntry.alias.targetCommand.trim().isEmpty())) {
                entries.add(new AliasEntry(new Alias()));
                buildEntryWidgets();
            }
        }
    }

    private void save() {
        updateEntriesFromFields();

        PersistentData.get().aliases.clear();

        for (AliasEntry entry : entries) {
            // Skip empty entries
            if (entry.alias.aliasCommand.trim().isEmpty() || entry.alias.targetCommand.trim().isEmpty()) {
                continue;
            }


            String aliasCommand = entry.alias.aliasCommand.trim();
            String targetCommand = entry.alias.targetCommand.trim();

            if (aliasCommand == targetCommand) {
                continue;
            }

            if (aliasCommand.startsWith("/")) {
                aliasCommand = aliasCommand.substring(1).trim();
            }
            if (targetCommand.startsWith("/")) {
                targetCommand = targetCommand.substring(1).trim();
            }

            // Create cleaned alias
            Alias cleanedAlias = new Alias();
            cleanedAlias.aliasCommand = aliasCommand;
            cleanedAlias.targetCommand = targetCommand;
            cleanedAlias.enabled = entry.alias.enabled;

            PersistentData.get().aliases.add(cleanedAlias);
        }

        // Remove circular references (alias pointing to another alias)
        PersistentData.get().aliases.removeIf(alias -> {
            for (Alias other : PersistentData.get().aliases) {
                if (!alias.equals(other) && alias.targetCommand.equals(other.aliasCommand)) {
                    return true;
                }
            }
            return false;
        });

        PersistentData.save();
    }

    @Override
    public void close() {
        animation.backwards();
        TickSchedulerHelper.runAfter(() -> {
            if (client != null) {
                client.setScreen(parent);
            }
        }, 10);
    }

    private static class AliasEntry {
        public final Alias alias;
        public TextBox aliasField;
        public TextBox targetField;
        public Checkbox enabledCheckbox;

        public AliasEntry(Alias alias) {
            this.alias = alias;
        }

        public boolean isEmpty() {
            return alias.aliasCommand.trim().isEmpty() && alias.targetCommand.trim().isEmpty();
        }
    }
}