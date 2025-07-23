package com.skyblock21.gui.components;

import com.skyblock21.util.ColorUtil;
import com.skyblock21.util.Render2DUtil;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import me.x150.renderer.render.ExtendedDrawContext;
import me.x150.renderer.util.Color;
import org.joml.Vector4f;

public class RoundedContainer extends FlowLayout {

    private final int rounding;
    private final Color color;

    public RoundedContainer(Sizing horizontalSizing, Sizing verticalSizing, int rounding, Color color, Algorithm algorithm) {
        super(horizontalSizing, verticalSizing, algorithm);
        this.rounding = rounding;
        this.color = color;
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        Vector4f radius = new Vector4f(rounding, rounding, rounding, rounding);
        ExtendedDrawContext.drawRoundedRect(
                context,
                x,
                y,
                width,
                height,
                radius,
                color
        );
//        Render2DUtil.drawRoundedRect(context, x, y, width, height, radius, color);
        this.drawChildren(context, mouseX, mouseY, partialTicks, delta, this.children);
    }


}
