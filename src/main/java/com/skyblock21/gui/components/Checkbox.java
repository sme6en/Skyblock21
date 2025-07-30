package com.skyblock21.gui.components;

import com.skyblock21.gui.Theme;
import com.skyblock21.gui.ThemeManager;
import com.skyblock21.util.ColorUtil;
import com.skyblock21.util.Render2DUtil;
import io.wispforest.owo.ui.component.CheckboxComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.util.function.BiConsumer;

public class Checkbox extends CheckboxComponent {

    private BiConsumer<Checkbox, Boolean> onToggle;

    public Checkbox(Text message) {
        super(message);
    }

    public Checkbox(Text message, BiConsumer<Checkbox, Boolean> onToggle) {
        super(message);
        this.onToggle = onToggle;
    }

        @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        Theme theme = ThemeManager.getCurrentTheme();
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        TextRenderer textRenderer = minecraftClient.textRenderer;

        int checkboxSize = getCheckboxSize(textRenderer);

        if (this.isChecked()) {
            Render2DUtil.drawRoundedBox(context, this.getX(), this.getY() + 2, checkboxSize, checkboxSize, 2, theme.primary);
        } else {
            Render2DUtil.drawOutlinedRoundedBox(context, this.getX(), this.getY() + 2, checkboxSize, checkboxSize, 2, theme.primary, theme.getSecondaryBackground());
        }

        if (!getMessage().getString().isEmpty()) {
            int textX = this.getX() + checkboxSize + 4;
            int textY = this.getY() + checkboxSize / 2 - theme.getTextRenderer().fontHeight / 2 + 2;

            context.drawText(theme.getTextRenderer(), getMessage(), textX, textY, ColorUtil.getIntFromColor(theme.text), false);
        }
    }

    @Override
    public Checkbox checked(boolean checked) {
        boolean wasChecked = this.isChecked();
        super.checked(checked);

        if (wasChecked != checked && onToggle != null) {
            onToggle.accept(this, checked);
        }

        return this;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible) {
            return false;
        }

        if (button == 0) {
            boolean wasChecked = this.isChecked();
            boolean result = super.mouseClicked(mouseX, mouseY, button);

            if (result && wasChecked != this.isChecked() && onToggle != null) {
                onToggle.accept(this, this.isChecked());
            }

            return result;
        }

        return false;
    }
}
