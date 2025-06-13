package com.skyblock21.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V"))
    private void pre_renderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
//            setShaderForHighlighting();
        RenderSystem.setShaderColor(1.0f, 0.0f, 0.0f, 0.7f);
    }

    @Inject(method = "renderCrosshair", at = @At(value = "TAIL"))
    private void post_renderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Unique
    private void setShaderForHighlighting() {
            var color = Color.RED.getRGB();
            float red = ((float) ((color >> 16) & 0xFF)) / 255F;
            float green = ((float) ((color >> 8) & 0xFF)) / 255F;
            float blue = ((float) (color & 0xFF)) / 255F;
            float alpha = 0.5F;
            RenderSystem.setShaderColor(red, green, blue, alpha);
        }
}

