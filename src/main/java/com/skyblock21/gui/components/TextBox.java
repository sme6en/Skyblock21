package com.skyblock21.gui.components;

import com.skyblock21.gui.Theme;
import com.skyblock21.gui.ThemeManager;
import com.skyblock21.util.ColorUtil;
import com.skyblock21.util.Render2DUtil;
import com.skyblock21.util.TextUtils;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.util.function.BiFunction;

public class TextBox extends TextBoxComponent {

    private BiFunction<String, Integer, OrderedText> colorPreviewProvider;
    private Text label;
    private boolean previewColors = false;

    public TextBox(Sizing horizontalSizing, Text label) {
        super(horizontalSizing);

        this.label = label;
        this.colorPreviewProvider = (text, startIndex) -> {
            String fullText = this.getText();
            String preservedFormatting = getActiveFormatting(fullText, startIndex);
            String textWithFormatting = applyPreservedFormatting(text, preservedFormatting);

            String dualPreviewText = createDualPreviewText(textWithFormatting);
            Text formattedText = TextUtils.fromLegacy(dualPreviewText);
            return formattedText.asOrderedText();
        };
    }

    private String createDualPreviewText(String text) {
        if (text == null || text.isEmpty()) return text;
        if (!previewColors) return text;

        StringBuilder result = new StringBuilder();
        String currentColor = null;

        for (int i = 0; i < text.length(); i++) {
            char currentChar = text.charAt(i);

            if (currentChar == '&' && i + 1 < text.length()) {
                char nextChar = Character.toLowerCase(text.charAt(i + 1));

                if (TextUtils.FORMAT_CODES.contains(nextChar)) {
                    result.append("§7&").append(text.charAt(i + 1));

                    if (isColorCode(nextChar)) {
                        currentColor = String.valueOf(nextChar);
                        result.append("§").append(nextChar);
                    } else if (isFormattingCode(nextChar)) {
                        if (currentColor != null) {
                            result.append("§").append(currentColor);
                        }
                        result.append("§").append(nextChar);
                    } else {
                        result.append("§").append(nextChar);
                        if (nextChar == 'r') {
                            currentColor = null;
                        }
                    }

                    i++;
                    continue;
                }
            }

            result.append(currentChar);
        }

        return result.toString();
    }

    private boolean isColorCode(char code) {
        return "0123456789abcdef".indexOf(code) != -1;
    }

    private boolean isFormattingCode(char code) {
        return "klmno".indexOf(code) != -1;
    }

    private String getActiveFormatting(String text, int position) {
        String activeColor = "";
        StringBuilder activeFormatting = new StringBuilder();

        for (int i = 0; i < position && i < text.length() - 1; i++) {
            if (text.charAt(i) == '§' || text.charAt(i) == '&') {
                char code = Character.toLowerCase(text.charAt(i + 1));

                if (isColorCode(code)) {
                    activeColor = "§" + code;
                    activeFormatting.setLength(0);
                } else if (isFormattingCode(code)) {
                    activeFormatting.append("§").append(code);
                } else if (code == 'r') {
                    activeColor = "";
                    activeFormatting.setLength(0);
                }
                i++;
            }
        }

        return activeColor + activeFormatting.toString();
    }

    private String applyPreservedFormatting(String visibleText, String preservedFormatting) {
        if (preservedFormatting.isEmpty()) {
            return visibleText;
        }

        if (visibleText.length() >= 2 && (visibleText.charAt(0) == '§' || visibleText.charAt(0) == '&')) {
            return visibleText;
        }

        return preservedFormatting + visibleText;
    }

    public TextBox setPreviewColors(boolean previewColors) {
        this.previewColors = previewColors;
        return this;
    }

    private int getAvailableTextWidth() {
        Theme theme = ThemeManager.getCurrentTheme();
        int labelWidth = theme.getTextRenderer().getWidth(this.label);
        int padding = 8; // 4 pixels on each side
        return this.width() - labelWidth - padding;
    }

    private void updateFirstCharacterIndex() {
        Theme theme = ThemeManager.getCurrentTheme();
        String fullText = this.getText();
        int availableWidth = getAvailableTextWidth();

        if (fullText.isEmpty()) {
            this.firstCharacterIndex = 0;
            return;
        }

        if (fullText.length() > this.getMaxLength()) {
            return;
        }

        if (theme.getTextRenderer().getWidth(fullText) <= availableWidth) {
            this.firstCharacterIndex = 0;
            return;
        }

        int cursorPos = this.selectionStart;

        if (this.firstCharacterIndex <= cursorPos) {
            String currentView = fullText.substring(this.firstCharacterIndex, Math.min(fullText.length(), cursorPos + 1));
            if (theme.getTextRenderer().getWidth(currentView) <= availableWidth) {
                return;
            }
        }

        int bestStartIndex = cursorPos;

        for (int startIdx = Math.max(0, cursorPos - 20); startIdx <= cursorPos; startIdx++) {
            String testText = fullText.substring(startIdx, Math.min(fullText.length(), cursorPos + 1));
            if (theme.getTextRenderer().getWidth(testText) <= availableWidth) {
                bestStartIndex = startIdx;
                break;
            }
        }

        this.firstCharacterIndex = bestStartIndex;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        Theme theme = ThemeManager.getCurrentTheme();

        if (this.isVisible()) {
            updateFirstCharacterIndex();

            Render2DUtil.drawBox(context, getX(), getY(), getWidth(), getHeight(), theme.getSecondaryBackground());
            Render2DUtil.drawLine(context, getX(), getY() + getHeight(), getX() + getWidth(), getY() + getHeight(), theme.primary);

            context.drawText(theme.getTextRenderer(), this.label,
                    this.getX() + this.width() - theme.getTextRenderer().getWidth(label) - 4,
                    this.getY() + (theme.getTextRenderer().fontHeight / 2) + 2,
                    ColorUtil.getIntFromColor(theme.primary), false);

            int j = this.selectionStart - this.firstCharacterIndex;
            int availableWidth = getAvailableTextWidth();

            String workingText = this.getText();
            if (workingText.length() > this.getMaxLength()) {
                workingText = workingText.substring(0, this.getMaxLength());
                if (this.firstCharacterIndex > workingText.length()) {
                    this.firstCharacterIndex = Math.max(0, workingText.length() - 1);
                }
            }

            String string = theme.getTextRenderer().trimToWidth(
                    workingText.substring(this.firstCharacterIndex), availableWidth);

            boolean bl = j >= 0 && j <= string.length();
            boolean bl2 = this.isFocused() && (Util.getMeasuringTimeMs() - this.lastSwitchFocusTime) / 300L % 2L == 0L && bl;

            int k = this.getX() + 4;
            int l = this.getY() + (this.height - 8) / 2;
            int m = k;
            int n = MathHelper.clamp(this.selectionEnd - this.firstCharacterIndex, 0, string.length());

            if (!string.isEmpty()) {
                String string2 = bl ? string.substring(0, j) : string;
                m = context.drawText(theme.getTextRenderer(),
                        (OrderedText) colorPreviewProvider.apply(string2, this.firstCharacterIndex),
                        k, l, ColorUtil.getIntFromColor(theme.text), false);
            }

            boolean bl3 = this.selectionStart < this.getText().length() || this.getText().length() >= this.getMaxLength();
            int o = m;
            if (!bl) {
                o = j > 0 ? k + this.width : k;
            } else if (bl3) {
                o = m - 1;
                --m;
            }

            if (!string.isEmpty() && bl && j < string.length()) {
                int remainingTextStartIndex = this.firstCharacterIndex + j;
                context.drawText(theme.getTextRenderer(),
                        (OrderedText) this.colorPreviewProvider.apply(string.substring(j), remainingTextStartIndex),
                        m, l, ColorUtil.getIntFromColor(theme.text), false);
            }

            if (this.placeholder != null && string.isEmpty() && !this.isFocused()) {
                context.drawText(theme.getTextRenderer(), this.placeholder, m, l,
                        ColorUtil.getIntFromColor(theme.getTextSecondary()), false);
            }

            if (bl2) {
                if (bl3) {
                    RenderLayer var10001 = RenderLayer.getGuiOverlay();
                    int var10003 = l - 1;
                    int var10004 = o + 1;
                    int var10005 = l + 1;
                    context.fill(var10001, o, var10003, var10004, var10005 + 9, -3092272);
                } else {
                    context.drawTextWithShadow(theme.getTextRenderer(), "_", o, l,
                            ColorUtil.getIntFromColor(theme.secondary));
                }
            }

            if (n != j) {
                int p = k + theme.getTextRenderer().getWidth(string.substring(0, n));
                int var19 = l - 1;
                int var20 = p - 1;
                int var21 = l + 1;
                this.drawSelectionHighlight(context, o, var19, var20, var21 + 9);
            }
        }
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.getText().length() >= this.getMaxLength() && this.selectionStart == this.selectionEnd) {
            return false;
        }

        boolean result = super.charTyped(chr, modifiers);
        if (result) {
            updateFirstCharacterIndex();
        }
        return result;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean result = super.keyPressed(keyCode, scanCode, modifiers);
        updateFirstCharacterIndex();
        return result;
    }
}