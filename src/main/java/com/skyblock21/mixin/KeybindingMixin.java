package com.skyblock21.mixin;

import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.features.keyshortcuts.KeyShortcuts;
import com.skyblock21.features.keyshortcuts.Shortcut;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public class KeybindingMixin {

    @Inject(method = "equals", at = @At("HEAD"), cancellable = true)
    private void sb21$checkCustomShortcutEquals(KeyBinding other, CallbackInfoReturnable<Boolean> cir) {
            KeyBinding thisBinding = (KeyBinding) (Object) this;

            if (conflictsWithCustomShortcut(thisBinding) || conflictsWithCustomShortcut(other)) {
                if (thisBinding.getDefaultKey().equals(other.getDefaultKey())) {
                    cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "matchesKey", at = @At("RETURN"))
    private void sb21$checkCustomShortcutConflict(int keyCode, int scanCode, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            KeyBinding thisBinding = (KeyBinding) (Object) this;
            conflictsWithCustomShortcut(thisBinding);
        }
    }

    @Unique
    private boolean conflictsWithCustomShortcut(KeyBinding keyBinding) {
        InputUtil.Key key = keyBinding.getDefaultKey();
        int keyCode = key.getCode();

        for (Shortcut shortcut : PersistentData.get().shortcuts) {
            if (!shortcut.enabled || shortcut.keyCode == 0) continue;

            if (shortcut.keyCode < 1000 && keyCode == shortcut.keyCode) {
                return true;
            }

            if (shortcut.keyCode >= 1000) {
                int mouseButton = shortcut.keyCode - 1000;
                if (key.getCategory() == InputUtil.Type.MOUSE && keyCode == -(100 + mouseButton)) {
                    return true;
                }
            }
        }

        return false;
    }
}
