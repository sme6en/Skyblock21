package com.skyblock21.features.commandaliases;

import com.mojang.brigadier.CommandDispatcher;
import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.mixin.CommandDispatcherMixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.ArrayList;

import static net.minecraft.text.Text.literal;

public class CommandAliasesScreen extends Screen {
    private final Screen parent;
    private final List<AliasEntry> entries = new ArrayList<>();
    private int scrollOffset = 0;
    private final int entryHeight = 35;
    private int maxVisibleEntries;
    private final int listStartY = 100;
    private int listEndY;
    private int scrollbarX;
    private final int scrollbarWidth = 8;
    private boolean isDraggingScrollbar = false;
    private int dragStartY = 0;
    private int dragStartOffset = 0;

    public CommandAliasesScreen(Screen parent) {
        super(literal("Command Aliases"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int paddingX = this.width / 10;
        int paddingY = this.height / 10;

        this.listEndY = this.height - paddingY - 80;
        this.scrollbarX = this.width - paddingX - scrollbarWidth - 10;

        int availableHeight = listEndY - listStartY;
        this.maxVisibleEntries = Math.max(5, availableHeight / entryHeight);

        entries.clear();

        for (Alias alias : PersistentData.get().aliases) {
            entries.add(new AliasEntry(alias));
        }

        if (entries.isEmpty() || !entries.get(entries.size() - 1).isEmpty()) {
            entries.add(new AliasEntry(new Alias()));
        }

        initializeWidgets();

        int buttonY = this.height - paddingY - 25;
        int buttonWidth = 100;
        int buttonSpacing = 20;
        int totalButtonWidth = (buttonWidth * 2) + buttonSpacing;
        int buttonStartX = (this.width - totalButtonWidth) / 2;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> {
            saveAndClose();
        }).dimensions(buttonStartX, buttonY, buttonWidth, 25).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Add New"), button -> {
            // Save current entries before adding new one
            updateEntriesFromFields();
            entries.add(new AliasEntry(new Alias()));
            initializeWidgets();
        }).dimensions(buttonStartX + buttonWidth + buttonSpacing, buttonY, buttonWidth, 25).build());
    }

    private void initializeWidgets() {
        clearChildren();

        int paddingX = this.width / 10;
        int entryWidth = this.width - (paddingX * 2) - scrollbarWidth - 20;

        for (int i = 0; i < entries.size(); i++) {
            if (i < scrollOffset || i >= scrollOffset + maxVisibleEntries) continue;

            AliasEntry entry = entries.get(i);
            int y = listStartY + (i - scrollOffset) * entryHeight;

            int checkboxX = paddingX;
            int aliasX = checkboxX + 30;
            int aliasWidth = (int)(entryWidth * 0.2);
            int targetX = aliasX + aliasWidth + 10;
            int targetWidth = (int)(entryWidth * 0.55);
            int removeX = targetX + targetWidth + 10;
            int removeWidth = 80;

            entry.enabledBox = CheckboxWidget.builder(Text.literal(""), this.textRenderer)
                                             .pos(checkboxX, y + 8)
                                             .checked(entry.alias.enabled)
                                             .build();
            addDrawableChild(entry.enabledBox);

            entry.aliasField = new TextFieldWidget(this.textRenderer, aliasX, y, aliasWidth, 25, Text.literal("Alias"));
            entry.aliasField.setText(entry.alias.aliasCommand);
            entry.aliasField.setMaxLength(64);
            addDrawableChild(entry.aliasField);

            entry.targetField = new TextFieldWidget(this.textRenderer, targetX, y, targetWidth, 25, Text.literal("Target Command"));
            entry.targetField.setText(entry.alias.targetCommand);
            entry.targetField.setMaxLength(512);
            addDrawableChild(entry.targetField);

            final int index = i;
            ButtonWidget removeButton = ButtonWidget.builder(Text.literal("Remove"), button -> {
                updateEntriesFromFields();
                entries.remove(index);
                int maxOffset = Math.max(0, entries.size() - maxVisibleEntries);
                scrollOffset = Math.min(scrollOffset, maxOffset);
                initializeWidgets();
            }).dimensions(removeX, y, removeWidth, 25).build();
            addDrawableChild(removeButton);
        }

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
            entries.add(new AliasEntry(new Alias()));
            initializeWidgets();
        }).dimensions(buttonStartX + buttonWidth + buttonSpacing, buttonY, buttonWidth, 25).build());
    }

    private void updateEntriesFromFields() {
        for (AliasEntry entry : entries) {
            if (entry.aliasField != null) {
                entry.alias.aliasCommand = entry.aliasField.getText().trim();
            }
            if (entry.targetField != null) {
                entry.alias.targetCommand = entry.targetField.getText().trim();
            }
            if (entry.enabledBox != null) {
                entry.alias.enabled = entry.enabledBox.isChecked();
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int paddingX = this.width / 10;
        int paddingY = this.height / 10;

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, paddingY / 2, 0xFFFFFF);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Create simple command aliases like: sb → skyblock").formatted(Formatting.GRAY),
                this.width / 2, paddingY / 2 + 15, 0xCCCCCC);

        int checkboxX = paddingX;
        int aliasX = checkboxX + 60;
        int targetX = aliasX + (int)((this.width - (paddingX * 2) - scrollbarWidth - 20) * 0.2) + 10;

        context.drawTextWithShadow(this.textRenderer, "Enabled", checkboxX, listStartY - 20, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "Alias Command", aliasX, listStartY - 20, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "Target Command", targetX, listStartY - 20, 0xFFFFFF);

        context.fill(paddingX - 5, listStartY - 5, scrollbarX + scrollbarWidth + 5, listEndY + 5, 0x80000000);

        if (entries.size() > maxVisibleEntries) {
            renderScrollbar(context, mouseX, mouseY);
        }

        String countText = String.format("Aliases: %d | Showing: %d-%d",
                entries.size(),
                entries.size() > 0 ? scrollOffset + 1 : 0,
                Math.min(scrollOffset + maxVisibleEntries, entries.size()));
        context.drawTextWithShadow(this.textRenderer, countText, paddingX, this.height - paddingY - 65, 0xCCCCCC);

        context.drawTextWithShadow(this.textRenderer,
                Text.literal("Examples: sb → skyblock | h → home | warp → warp hub").formatted(Formatting.DARK_GRAY),
                paddingX, this.height - paddingY - 50, 0x888888);
    }

    private void renderScrollbar(DrawContext context, int mouseX, int mouseY) {
        int totalEntries = entries.size();
        int scrollbarHeight = listEndY - listStartY;

        context.fill(scrollbarX, listStartY, scrollbarX + scrollbarWidth, listEndY, 0x40FFFFFF);

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
        if (button == 0 && entries.size() > maxVisibleEntries) {
            if (mouseX >= scrollbarX && mouseX <= scrollbarX + scrollbarWidth &&
                    mouseY >= listStartY && mouseY <= listEndY) {

                int totalEntries = entries.size();
                int scrollbarHeight = listEndY - listStartY;

                float thumbRatio = (float) maxVisibleEntries / totalEntries;
                int thumbHeight = Math.max(10, (int) (scrollbarHeight * thumbRatio));

                float scrollRatio = (float) scrollOffset / (totalEntries - maxVisibleEntries);
                int thumbY = listStartY + (int) ((scrollbarHeight - thumbHeight) * scrollRatio);

                if (mouseY >= thumbY && mouseY <= thumbY + thumbHeight) {
                    isDraggingScrollbar = true;
                    dragStartY = (int) mouseY;
                    dragStartOffset = scrollOffset;
                    return true;
                } else {
                    // Jump to position
                    updateEntriesFromFields(); // Save before jumping
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
        PersistentData.get().aliases.clear();

        for (AliasEntry entry : entries) {
            if (entry.aliasField != null) {
                entry.alias.aliasCommand = entry.aliasField.getText().trim();
            }
            if (entry.targetField != null) {
                entry.alias.targetCommand = entry.targetField.getText().trim();
            }
            if (entry.enabledBox != null) {
                entry.alias.enabled = entry.enabledBox.isChecked();
            }

            if (!entry.alias.aliasCommand.trim().isEmpty() && !entry.alias.targetCommand.trim().isEmpty()) {

                if (entry.alias.aliasCommand.startsWith("/")) {
                    entry.alias.aliasCommand = entry.alias.aliasCommand.substring(1).trim();
                }

                if (entry.alias.targetCommand.startsWith("/")) {
                    entry.alias.targetCommand = entry.alias.targetCommand.substring(1).trim();
                }

                PersistentData.get().aliases.add(entry.alias);
            }


        }

        PersistentData.get().aliases.removeIf(alias -> {
            for (Alias other : PersistentData.get().aliases) {
                if (!alias.equals(other) && alias.targetCommand.equals(other.aliasCommand)) {
                    return true;
                }
            }
            return false;
        });

        this.client.setScreen(parent);
    }

    @Override
    public void close() {
        saveAndClose();
    }

    private static class AliasEntry {
        public final Alias alias;
        public TextFieldWidget aliasField;
        public TextFieldWidget targetField;
        public CheckboxWidget enabledBox;

        public AliasEntry(Alias alias) {
            this.alias = alias;
        }

        public boolean isEmpty() {
            return alias.aliasCommand.trim().isEmpty() && alias.targetCommand.trim().isEmpty();
        }
    }
}
