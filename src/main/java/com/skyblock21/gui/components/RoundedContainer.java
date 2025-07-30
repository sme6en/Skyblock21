package com.skyblock21.gui.components;

import com.skyblock21.util.ColorUtil;
import com.skyblock21.util.Render2DUtil;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import org.joml.Vector4f;

import java.awt.*;

public class RoundedContainer extends FlowLayout {

    private final float rounding;
    private final Color color;

    public RoundedContainer(Sizing horizontalSizing, Sizing verticalSizing, float rounding, Color color, Algorithm algorithm) {
        super(horizontalSizing, verticalSizing, algorithm);
        this.rounding = rounding;
        this.color = color;
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        Render2DUtil.drawRoundedBox(context, x, y, width, height, rounding, color);
        this.drawChildren(context, mouseX, mouseY, partialTicks, delta, this.children);
    }

}
