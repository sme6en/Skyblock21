package com.skyblock21.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class WindowEvents {


    public static Event<OnCloseWindow> CLOSE_WINDOW = EventFactory.createArrayBacked(OnCloseWindow.class, listeners -> (screenTitle, screenHandler) -> {
        for (OnCloseWindow listener : listeners) {
            boolean shouldClose = listener.onCloseWindow(screenTitle, screenHandler);

            if (!shouldClose) {
                return false;
            }
        }

        return true;
    });

    public static Event<OnDrawSlot> DRAW_BEFORE_ITEM = EventFactory.createArrayBacked(OnDrawSlot.class, listeners -> (screenTitle, context, slot) -> {
        for (OnDrawSlot listener : listeners) {
            listener.onDrawSlot(screenTitle, context, slot);
        }
    });

    public static Event<OnSlotClick> SLOT_CLICK = EventFactory.createArrayBacked(OnSlotClick.class, listeners -> (handledScreen, handler, slot) -> {
        for (OnSlotClick listener : listeners) {
            boolean shouldClick = listener.onSlotClick(handledScreen, handler, slot);

            if (!shouldClick) {
                return false; // Cancel the slot click if any listener returns true
            }
        }

        return true;
    });


    @FunctionalInterface
    public interface OnCloseWindow {
        boolean onCloseWindow(HandledScreen<?> handledScreen, ScreenHandler handler);
    }

    @FunctionalInterface
    public interface OnDrawSlot {
        void onDrawSlot(Text screenTitle, DrawContext context, Slot slot);
    }

    @FunctionalInterface
    public interface OnSlotClick {
        boolean onSlotClick(HandledScreen<?> handledScreen, ScreenHandler handler, Slot slot);
    }

}
