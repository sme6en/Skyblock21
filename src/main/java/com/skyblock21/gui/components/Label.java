package com.skyblock21.gui.components;

import com.skyblock21.gui.ThemeManager;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

public class Label extends LabelComponent {

    public Label(Text text) {
        super(text);
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        var matrices = context.getMatrices();
        TextRenderer textRenderer = ThemeManager.getCurrentTheme().getTextRenderer();

        matrices.push();
        matrices.translate(0, 1 / MinecraftClient.getInstance().getWindow().getScaleFactor(), 0);

        int x = this.x;
        int y = this.y;

        if (this.horizontalSizing.get().isContent()) {
            x += this.horizontalSizing.get().value;
        }
        if (this.verticalSizing.get().isContent()) {
            y += this.verticalSizing.get().value;
        }

        switch (this.verticalTextAlignment) {
            case CENTER -> y += (this.height - (this.textHeight())) / 2;
            case BOTTOM -> y += this.height - (this.textHeight());
        }

        final int lambdaX = x;
        final int lambdaY = y;

        context.draw((vertexConsumerProvider) -> {
            for (int i = 0; i < this.wrappedText.size(); i++) {
                var renderText = this.wrappedText.get(i);
                int renderX = lambdaX;

                switch (this.horizontalTextAlignment) {
                    case CENTER -> renderX += (this.width - textRenderer.getWidth(renderText)) / 2;
                    case RIGHT -> renderX += this.width - textRenderer.getWidth(renderText);
                }

                int renderY = lambdaY + i * (this.lineHeight() + this.lineSpacing());
                renderY += this.lineHeight() - textRenderer.fontHeight;

                context.drawText(textRenderer, renderText, renderX, renderY, this.color.get().argb(), this.shadow);
            }
        });

        matrices.pop();
    }

}
