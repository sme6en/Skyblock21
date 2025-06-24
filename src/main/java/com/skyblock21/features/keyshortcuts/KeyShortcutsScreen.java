package com.skyblock21.features.keyshortcuts;

import com.skyblock21.config.persistent.PersistentData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class KeyShortcutsScreen extends Screen {
    private final Screen parent;
    private final List<ShortcutEntry> entries = new ArrayList<>();
    private int scrollOffset = 0;
    private final int entryHeight = 30;
    private int maxVisibleEntries;
    private final int listStartY = 80;
    private int listEndY;
    private int scrollbarX;
    private final int scrollbarWidth = 8;
    private KeybindButton listeningButton = null;
    private boolean isDraggingScrollbar = false;
    private int dragStartY = 0;
    private int dragStartOffset = 0;

    public KeyShortcutsScreen(Screen parent) {
        super(Text.literal("Key Shortcuts"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // Calculate screen dimensions with 10% padding
        int paddingX = this.width / 10;
        int paddingY = this.height / 10;

        // Update dynamic values based on screen size
        this.listEndY = this.height - paddingY - 60; // 60px for bottom buttons
        this.scrollbarX = this.width - paddingX - scrollbarWidth - 10;

        // Calculate how many entries can fit
        int availableHeight = listEndY - listStartY;
        this.maxVisibleEntries = Math.max(5, availableHeight / entryHeight); // Minimum 5 entries

        entries.clear();

        // Load existing shortcuts
        for (Shortcut shortcut : PersistentData.get().shortcuts) {
            entries.add(new ShortcutEntry(shortcut));
        }

        // Add empty entry for new shortcut
        if (entries.isEmpty() || !entries.get(entries.size() - 1).isEmpty()) {
            entries.add(new ShortcutEntry(new Shortcut()));
        }

        initializeWidgets();

        // Bottom buttons - centered and properly spaced
        int buttonY = this.height - paddingY - 25;
        int buttonWidth = 100;
        int buttonSpacing = 20;
        int totalButtonWidth = (buttonWidth * 2) + buttonSpacing;
        int buttonStartX = (this.width - totalButtonWidth) / 2;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> {
            saveAndClose();
        }).dimensions(buttonStartX, buttonY, buttonWidth, 25).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Add New"), button -> {
            entries.add(new ShortcutEntry(new Shortcut()));
            initializeWidgets();
        }).dimensions(buttonStartX + buttonWidth + buttonSpacing, buttonY, buttonWidth, 25).build());
    }

    private void initializeWidgets() {
        clearChildren();

        // Calculate layout with padding
        int paddingX = this.width / 10;
        int entryWidth = this.width - (paddingX * 2) - scrollbarWidth - 20;

        for (int i = 0; i < entries.size(); i++) {
            if (i < scrollOffset || i >= scrollOffset + maxVisibleEntries) continue;

            ShortcutEntry entry = entries.get(i);
            int y = listStartY + (i - scrollOffset) * entryHeight;

            // Calculate component positions and sizes
            int checkboxX = paddingX + 20;
            int commandX = checkboxX + 50;
            int commandWidth = (int)(entryWidth * 0.4);
            int keybindX = commandX + commandWidth + 10;
            int keybindWidth = (int)(entryWidth * 0.35);
            int removeX = keybindX + keybindWidth + 10;
            int removeWidth = 80;

            // Enable checkbox
            entry.enabledBox = CheckboxWidget.builder(Text.literal(""), this.textRenderer)
                                             .pos(checkboxX, y + 3)
                                             .checked(entry.shortcut.enabled)
                                             .build();
            addDrawableChild(entry.enabledBox);

            // Command text field
            entry.commandField = new TextFieldWidget(this.textRenderer, commandX, y, commandWidth, 25, Text.literal("Command"));
            entry.commandField.setText(entry.shortcut.command);
            entry.commandField.setMaxLength(256);
            addDrawableChild(entry.commandField);

            // Keybind button
            entry.keybindButton = new KeybindButton(keybindX, y, keybindWidth, 25, entry);
            addDrawableChild(entry.keybindButton);

            // Remove button
            final int index = i;
            ButtonWidget removeButton = ButtonWidget.builder(Text.literal("Remove"), button -> {
                entries.remove(index);
                // Adjust scroll offset if needed
                int maxOffset = Math.max(0, entries.size() - maxVisibleEntries);
                scrollOffset = Math.min(scrollOffset, maxOffset);
                initializeWidgets();
            }).dimensions(removeX, y, removeWidth, 25).build();
            addDrawableChild(removeButton);
        }

        // Re-add bottom buttons
        int paddingY = this.height / 10;
        int buttonY = this.height - paddingY - 25;
        int buttonWidth = 100;
        int buttonSpacing = 20;
        int totalButtonWidth = (buttonWidth * 2) + buttonSpacing;
        int buttonStartX = (this.width - totalButtonWidth) / 2;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> {
            saveAndClose();
        }).dimensions(buttonStartX, buttonY, buttonWidth, 25).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Add New"), button -> {
            updateEntriesFromFields();
            entries.add(new ShortcutEntry(new Shortcut()));
            initializeWidgets();
        }).dimensions(buttonStartX + buttonWidth + buttonSpacing, buttonY, buttonWidth, 25).build());
    }

    private void updateEntriesFromFields() {
        // Update all entry data from their current GUI fields
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Calculate padding
        int paddingX = this.width / 10;
        int paddingY = this.height / 10;

        // Title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, paddingY / 2, 0xFFFFFF);

        // Column headers
        int checkboxX = paddingX + 20;
        int commandX = checkboxX + 60;
        int keybindX = commandX + (int)((this.width - (paddingX * 2) - scrollbarWidth - 20) * 0.4) + 10;

        context.drawTextWithShadow(this.textRenderer, "Enabled", checkboxX, listStartY - 20, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "Command", commandX, listStartY - 20, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "Keybind", keybindX, listStartY - 20, 0xFFFFFF);

        // Render list background
        context.fill(paddingX - 5, listStartY - 5, scrollbarX + scrollbarWidth + 5, listEndY + 5, 0x80000000);


        // Render scrollbar if needed
        if (entries.size() > maxVisibleEntries) {
            renderScrollbar(context, mouseX, mouseY);
        }

        // Show entry count
        String countText = String.format("Entries: %d | Showing: %d-%d",
                entries.size(),
                entries.size() > 0 ? scrollOffset + 1 : 0,
                Math.min(scrollOffset + maxVisibleEntries, entries.size()));
        context.drawTextWithShadow(this.textRenderer, countText, paddingX, this.height - paddingY - 45, 0xCCCCCC);

    }

    private void renderScrollbar(DrawContext context, int mouseX, int mouseY) {
        int totalEntries = entries.size();
        int scrollbarHeight = listEndY - listStartY;

        // Background track
        context.fill(scrollbarX, listStartY, scrollbarX + scrollbarWidth, listEndY, 0x40FFFFFF);

        // Calculate thumb size and position
        float thumbRatio = (float) maxVisibleEntries / totalEntries;
        int thumbHeight = Math.max(10, (int) (scrollbarHeight * thumbRatio));

        float scrollRatio = (float) scrollOffset / (totalEntries - maxVisibleEntries);
        int thumbY = listStartY + (int) ((scrollbarHeight - thumbHeight) * scrollRatio);

        // Thumb
        boolean isHovered = mouseX >= scrollbarX && mouseX <= scrollbarX + scrollbarWidth &&
                mouseY >= thumbY && mouseY <= thumbY + thumbHeight;
        int thumbColor = isDraggingScrollbar ? 0xFFFFFFFF : (isHovered ? 0xC0FFFFFF : 0x80FFFFFF);
        context.fill(scrollbarX, thumbY, scrollbarX + scrollbarWidth, thumbY + thumbHeight, thumbColor);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listeningButton != null) {

            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                // Clear binding - create new HashSet instead of clearing
                listeningButton.entry.shortcut.keyCode = 0;
                listeningButton.entry.shortcut.modifiers = new HashSet<>();
            } else if (isModifierKey(keyCode)) {
                // Don't allow modifier keys to be set as the main key
                // Just ignore the input and stay listening
                return true;
            } else {
                // Set new binding - create new HashSet
                listeningButton.entry.shortcut.keyCode = keyCode;
                listeningButton.entry.shortcut.modifiers = new HashSet<>();

                if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                    listeningButton.entry.shortcut.modifiers.add(Modifier.CTRL);
                }
                if ((modifiers & GLFW.GLFW_MOD_ALT) != 0) {
                    listeningButton.entry.shortcut.modifiers.add(Modifier.ALT);
                }
                if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) {
                    listeningButton.entry.shortcut.modifiers.add(Modifier.SHIFT);
                }
            }
            listeningButton = null;
            return true;
        }

        // Handle scrolling with arrow keys
        if (keyCode == GLFW.GLFW_KEY_UP) {
            scroll(-1);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
            scroll(1);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_PAGE_UP) {
            scroll(-5);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            scroll(5);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_HOME) {
            scrollOffset = 0;
            initializeWidgets();
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_END) {
            scrollOffset = Math.max(0, entries.size() - maxVisibleEntries);
            initializeWidgets();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
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

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (entries.size() > maxVisibleEntries) {
            scroll(-(int) verticalAmount * 3);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (listeningButton != null) {

            listeningButton.entry.shortcut.keyCode = button + 1000;
            listeningButton.entry.shortcut.modifiers = new HashSet<>();

            long window = MinecraftClient.getInstance().getWindow().getHandle();
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS ||
                    GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS) {
                listeningButton.entry.shortcut.modifiers.add(Modifier.CTRL);
            }
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS ||
                    GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS) {
                listeningButton.entry.shortcut.modifiers.add(Modifier.ALT);
            }
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                    GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS) {
                listeningButton.entry.shortcut.modifiers.add(Modifier.SHIFT);
            }

            listeningButton = null;

            return true;
        }

        if (button == 0 && entries.size() > maxVisibleEntries) {
            // Check if clicking on scrollbar
            if (mouseX >= scrollbarX && mouseX <= scrollbarX + scrollbarWidth &&
                    mouseY >= listStartY && mouseY <= listEndY) {

                // Calculate click position
                int totalEntries = entries.size();
                int scrollbarHeight = listEndY - listStartY;

                float thumbRatio = (float) maxVisibleEntries / totalEntries;
                int thumbHeight = Math.max(10, (int) (scrollbarHeight * thumbRatio));

                float scrollRatio = (float) scrollOffset / (totalEntries - maxVisibleEntries);
                int thumbY = listStartY + (int) ((scrollbarHeight - thumbHeight) * scrollRatio);

                if (mouseY >= thumbY && mouseY <= thumbY + thumbHeight) {
                    // Start dragging
                    isDraggingScrollbar = true;
                    dragStartY = (int) mouseY;
                    dragStartOffset = scrollOffset;
                    return true;
                } else {
                    updateEntriesFromFields();
                    float clickRatio = (float) (mouseY - listStartY) / scrollbarHeight;
                    int newOffset = (int) (clickRatio * (totalEntries - maxVisibleEntries));
                    scrollOffset = MathHelper.clamp(newOffset, 0, totalEntries - maxVisibleEntries);
                    initializeWidgets();
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDraggingScrollbar = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDraggingScrollbar && entries.size() > maxVisibleEntries) {
            updateEntriesFromFields();
            int dragDistance = (int) mouseY - dragStartY;
            int scrollbarHeight = listEndY - listStartY;
            int totalEntries = entries.size();

            float thumbRatio = (float) maxVisibleEntries / totalEntries;
            int thumbHeight = Math.max(10, (int) (scrollbarHeight * thumbRatio));

            float dragRatio = (float) dragDistance / (scrollbarHeight - thumbHeight);
            int newOffset = dragStartOffset + (int) (dragRatio * (totalEntries - maxVisibleEntries));

            int oldOffset = scrollOffset;
            scrollOffset = MathHelper.clamp(newOffset, 0, totalEntries - maxVisibleEntries);

            if (oldOffset != scrollOffset) {
                initializeWidgets();
            }
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    private void scroll(int amount) {
        if (entries.size() <= maxVisibleEntries) return;

        updateEntriesFromFields();

        int oldOffset = scrollOffset;
        scrollOffset = MathHelper.clamp(scrollOffset + amount, 0, entries.size() - maxVisibleEntries);

        if (oldOffset != scrollOffset) {
            initializeWidgets();
        }
    }

    private void saveAndClose() {
        PersistentData.get().shortcuts.clear();

        for (ShortcutEntry entry : entries) {
            if (entry.commandField != null) {
                entry.shortcut.command = entry.commandField.getText();
            }
            if (entry.enabledBox != null) {
                entry.shortcut.enabled = entry.enabledBox.isChecked();
            }

            if (!entry.shortcut.command.trim().isEmpty() && entry.shortcut.keyCode != 0) {
                PersistentData.get().shortcuts.add(entry.shortcut);
            }
        }

        PersistentData.save();
        this.client.setScreen(parent);
    }

    @Override
    public void close() {
        saveAndClose();
    }

    private static class ShortcutEntry {
        public Shortcut shortcut;
        public TextFieldWidget commandField;
        public CheckboxWidget enabledBox;
        public KeybindButton keybindButton;

        public ShortcutEntry(Shortcut shortcut) {
            this.shortcut = shortcut;
        }

        public boolean isEmpty() {
            return shortcut.command.trim().isEmpty() && shortcut.keyCode == 0;
        }
    }

    private class KeybindButton extends ButtonWidget {
        private ShortcutEntry entry;

        public KeybindButton(int x, int y, int width, int height, ShortcutEntry entry) {
            super(x, y, width, height, Text.literal(""), button -> {}, DEFAULT_NARRATION_SUPPLIER);
            this.entry = entry;
        }

        @Override
        public Text getMessage() {
            if (listeningButton == this) {
                return Text.literal("Press a key...").formatted(Formatting.YELLOW);
            }

            if (entry.shortcut.keyCode == 0) {
                return Text.literal("Click to set").formatted(Formatting.GRAY);
            }

            return Text.literal(entry.shortcut.getDisplayString());
        }

        @Override
        public void onPress() {
            listeningButton = this;
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);

            // Show conflict warning if key is already used
            if (entry.shortcut.keyCode != 0 && listeningButton != this) {
                boolean hasConflict = false;
                for (ShortcutEntry other : entries) {
                    if (other != entry &&
                            other.shortcut.keyCode == entry.shortcut.keyCode &&
                            other.shortcut.modifiers.equals(entry.shortcut.modifiers)) {
                        hasConflict = true;
                        break;
                    }
                }

                if (hasConflict) {
                    context.drawText(textRenderer, "!", getX() + getWidth() + 5, getY() + 6, 0xFF0000, false);
                }
            }
        }
    }
}