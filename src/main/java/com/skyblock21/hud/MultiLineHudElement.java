package com.skyblock21.hud;

import com.google.gson.*;
import com.skyblock21.util.Location;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.*;
import java.util.stream.Collectors;

public abstract class MultiLineHudElement extends HudElement {

    protected final List<HudLine> lines = new ArrayList<>();
    protected final Map<String, HudGroup> groups = new HashMap<>();
    protected int maxWidth = 0;
    protected int totalHeight = 0;

    public MultiLineHudElement(int x, int y) {
        super(x, y);
    }

    public MultiLineHudElement(int x, int y, Location location) {
        super(x, y, location);
    }

    public MultiLineHudElement(int x, int y, Location location, boolean alwaysDummy) {
        super(x, y, location, alwaysDummy);
    }

    // Enhanced line addition methods
    public void addLine(String id, Text content) {
        lines.add(new HudLine(id, content));
        recalculateDimensions();
    }

    public void addAmountLine(String id, String itemName, long amount) {
        lines.add(new HudAmountLine(id, itemName, amount));
        recalculateDimensions();
    }

    public void addAmountLine(String id, String itemName, long amount, String groupId) {
        lines.add(new HudAmountLine(id, itemName, amount, groupId));
        recalculateDimensions();
    }

    public void addLine(String id, Text content, String groupId) {
        lines.add(new HudLine(id, content, groupId));
        recalculateDimensions();
    }

    public void addLine(String id, Text content, String groupId, int order) {
        lines.add(new HudLine(id, content, groupId, order));
        recalculateDimensions();
    }

    // Group management with enhanced features
    public void createGroup(String id, String displayName, boolean alignAmounts) {
        groups.put(id, new HudGroup(id, displayName, 0, alignAmounts));
    }

    public void createGroup(String id, String displayName, int order, boolean alignAmounts) {
        groups.put(id, new HudGroup(id, displayName, order, alignAmounts));
    }

    public void setGroupSorting(String groupId, SortType sortType, boolean ascending) {
        HudGroup group = groups.get(groupId);
        if (group != null) {
            group.setSortType(sortType);
            group.setAscending(ascending);
            recalculateDimensions();
        }
    }

    // Existing methods remain the same...
    public void removeLine(String id) {
        lines.removeIf(line -> line.getId().equals(id));
        recalculateDimensions();
    }

    public void updateLine(String id, Text content) {
        if(lines.stream().noneMatch(l -> Objects.equals(l.getId(), id))) {
            addLine(id, content);
        }

        for (HudLine line : lines) {
            if (line.getId().equals(id)) {
                line.setContent(content);
                recalculateDimensions();
                return;
            }
        }
    }

    public void updateAmountLine(String id, String itemName, long amount) {
        if(lines.stream().noneMatch(l -> Objects.equals(l.getId(), id))) {
            addAmountLine(id, itemName, 0);
        }

        for (HudLine line : lines) {
            if (line.getId().equals(id) && line instanceof HudAmountLine amountLine) {
                amountLine.updateItemName(itemName);
                amountLine.updateAmount(amount);
                recalculateDimensions();
                return;
            }
        }
    }

    public void updateAmountLine(String id, long amount) {
        if(lines.stream().noneMatch(l -> Objects.equals(l.getId(), id))) {
            addAmountLine(id, id, 0);
        }

        for (HudLine line : lines) {
            if (line.getId().equals(id) && line instanceof HudAmountLine amountLine) {
                amountLine.updateAmount(amount);
                recalculateDimensions();
                return;
            }
        }
    }

    public List<HudLine> getVisibleLines() {
        // Group lines by their group ID
        Map<String, List<HudLine>> groupedLines = lines.stream()
                .filter(this::isLineVisible)
                .collect(Collectors.groupingBy(
                        line -> line.hasGroup() ? line.getGroupId() : "ungrouped"
                ));

        List<HudLine> sortedLines = new ArrayList<>();

        // Sort groups by order
        List<String> sortedGroupIds = groupedLines.keySet().stream()
                .sorted((a, b) -> {
                    if (a.equals("ungrouped")) return 1;
                    if (b.equals("ungrouped")) return -1;
                    HudGroup groupA = groups.get(a);
                    HudGroup groupB = groups.get(b);
                    return Integer.compare(
                            groupA != null ? groupA.getOrder() : 0,
                            groupB != null ? groupB.getOrder() : 0
                    );
                })
                .toList();

        // Sort lines within each group
        for (String groupId : sortedGroupIds) {
            List<HudLine> groupLines = groupedLines.get(groupId);

            if (groupId.equals("ungrouped")) {
                // Sort ungrouped lines by order
                groupLines.sort(Comparator.comparingInt(HudLine::getOrder));
            } else {
                // Sort grouped lines using group's comparator
                HudGroup group = groups.get(groupId);
                if (group != null) {
                    groupLines.sort(group.getComparator());
                }
            }

            sortedLines.addAll(groupLines);
        }

        return sortedLines;
    }

    private boolean isLineVisible(HudLine line) {
        if (!line.isEnabled()) return false;

        if (line.hasGroup()) {
            HudGroup group = groups.get(line.getGroupId());
            return group != null && group.isEnabled();
        }

        return true;
    }

    protected void recalculateDimensions() {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        if (textRenderer == null) return;
        List<HudLine> visibleLines = getVisibleLines();

        maxWidth = 0;
        totalHeight = 0;

        if (visibleLines.isEmpty()) {
            maxWidth = 100;
            totalHeight = textRenderer.fontHeight;
            return;
        }

        // Calculate dimensions considering alignment
        for (HudLine line : visibleLines) {
            if (line.getContent() != null) {
                int lineWidth;

                if (line instanceof HudAmountLine amountLine && line.hasGroup()) {
                    HudGroup group = groups.get(line.getGroupId());
                    if (group != null && group.isAlignAmounts()) {
                        // Calculate aligned width
                        int nameWidth = group.getMaxItemNameWidth(visibleLines);
                        int amountWidth = textRenderer.getWidth(" x" + HudAmountLine.formatAmount(amountLine.getAmount()));
                        lineWidth = nameWidth + amountWidth;
                    } else {
                        lineWidth = textRenderer.getWidth(line.getContent());
                    }
                } else {
                    lineWidth = textRenderer.getWidth(line.getContent());
                }

                maxWidth = Math.max(maxWidth, lineWidth);
            }
        }

        totalHeight = visibleLines.size() * textRenderer.fontHeight +
                (visibleLines.size() - 1) * VERTICAL_SPACING;

        maxWidth += HORIZONTAL_SPACING * 2;
        totalHeight += VERTICAL_SPACING * 2;
    }

    @Override
    protected void renderElement(DrawContext context) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        List<HudLine> visibleLines = getVisibleLines();

        int yOffset = VERTICAL_SPACING;

        for (HudLine line : visibleLines) {
            if (line.getContent() != null) {
                if (line instanceof HudAmountLine amountLine && line.hasGroup()) {
                    HudGroup group = groups.get(line.getGroupId());
                    if (group != null && group.isAlignAmounts()) {
                        renderAlignedAmountLine(context, textRenderer, amountLine, group, visibleLines, yOffset);
                    } else {
                        context.drawText(textRenderer, line.getContent(),
                                HORIZONTAL_SPACING, yOffset, 0xFFFFFF, false);
                    }
                } else {
                    context.drawText(textRenderer, line.getContent(),
                            HORIZONTAL_SPACING, yOffset, 0xFFFFFF, false);
                }
            }
            yOffset += textRenderer.fontHeight + VERTICAL_SPACING;
        }
    }

    private void renderAlignedAmountLine(DrawContext context, TextRenderer textRenderer,
                                         HudAmountLine amountLine, HudGroup group,
                                         List<HudLine> visibleLines, int yOffset) {
        int maxNameWidth = group.getMaxItemNameWidth(visibleLines);

        context.drawText(textRenderer, Text.literal(amountLine.getItemName()),
                HORIZONTAL_SPACING, yOffset, 0xFFFFFF, false);

        String amountText = "x" + HudAmountLine.formatAmount(amountLine.getAmount());
        context.drawText(textRenderer, Text.literal(amountText),
                HORIZONTAL_SPACING + maxNameWidth + 10, yOffset, 0xFFFFFF, false);
    }

    @Override
    protected void renderDummy(DrawContext context) {
        renderElement(context);
    }

    @Override
    public int getWidth() {
        return maxWidth;
    }

    @Override
    public int getHeight() {
        return totalHeight;
    }

    public JsonObject saveLineStates() {
        JsonObject root = new JsonObject();

        // Save line states
        JsonArray linesArray = new JsonArray();
        for (HudLine line : lines) {
            JsonObject lineObj = new JsonObject();
            lineObj.addProperty("id", line.getId());
            lineObj.addProperty("enabled", line.isEnabled());
            lineObj.addProperty("order", line.getOrder());
            lineObj.addProperty("type", line.getClass().getSimpleName());

            if (line.hasGroup()) {
                lineObj.addProperty("groupId", line.getGroupId());
            }
            linesArray.add(lineObj);
        }
        root.add("lines", linesArray);

        // Save group states
        JsonArray groupsArray = new JsonArray();
        for (HudGroup group : groups.values()) {
            groupsArray.add(group.saveToJson());
        }
        root.add("groups", groupsArray);

        return root;
    }

    public void loadLineStates(JsonObject data) {
        if (data == null) return;

        // Load group states
        if (data.has("groups")) {
            JsonArray groupsArray = data.getAsJsonArray("groups");
            for (JsonElement element : groupsArray) {
                JsonObject groupObj = element.getAsJsonObject();
                String groupId = groupObj.get("id").getAsString();
                HudGroup group = groups.get(groupId);
                if (group != null) {
                    group.loadFromJson(groupObj);
                }
            }
        }

        // Load line states
        if (data.has("lines")) {
            JsonArray linesArray = data.getAsJsonArray("lines");
            for (JsonElement element : linesArray) {
                JsonObject lineObj = element.getAsJsonObject();
                String lineId = lineObj.get("id").getAsString();
                HudLine line = getLine(lineId);
                if (line != null) {
                    line.setEnabled(lineObj.get("enabled").getAsBoolean());
                    if (lineObj.has("order")) {
                        line.setOrder(lineObj.get("order").getAsInt());
                    }
                    if (lineObj.has("groupId")) {
                        line.setGroupId(lineObj.get("groupId").getAsString());
                    }
                }
            }
        }

        recalculateDimensions();
    }

    // Helper methods
    public HudLine getLine(String id) {
        return lines.stream()
                .filter(line -> line.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void createGroup(String id, String displayName) {
        groups.put(id, new HudGroup(id, displayName));
    }

    public void createGroup(String id, String displayName, int order) {
        groups.put(id, new HudGroup(id, displayName, order));
    }

    public void toggleGroup(String groupId) {
        HudGroup group = groups.get(groupId);
        if (group != null) {
            group.setEnabled(!group.isEnabled());
            recalculateDimensions();
        }
    }

    public void setGroupEnabled(String groupId, boolean enabled) {
        HudGroup group = groups.get(groupId);
        if (group != null) {
            group.setEnabled(enabled);
            recalculateDimensions();
        }
    }

    public HudGroup getGroup(String groupId) {
        return groups.get(groupId);
    }

    public Collection<HudGroup> getGroups() {
        return groups.values();
    }

    public List<HudLine> getLines() {
        return new ArrayList<>(lines);
    }

    public void clearLines() {
        lines.clear();
        recalculateDimensions();
    }

    public void toggleLine(String id) {
        for (HudLine line : lines) {
            if (line.getId().equals(id)) {
                line.setEnabled(!line.isEnabled());
                recalculateDimensions();
                return;
            }
        }
    }

    public void setLineEnabled(String id, boolean enabled) {
        for (HudLine line : lines) {
            if (line.getId().equals(id)) {
                line.setEnabled(enabled);
                recalculateDimensions();
                return;
            }
        }
    }
}