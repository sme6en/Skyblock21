package com.skyblock21.mixin;

import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.features.keyshortcuts.KeyShortcuts;
import com.skyblock21.features.keyshortcuts.Shortcut;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ControlsListWidget.KeyBindingEntry.class)
public class KeyBindingEntryMixin {

    @Shadow
    @Final
    private KeyBinding binding;
    @Shadow
    private boolean duplicate;

    @Inject(method = "update", at = @At("TAIL"))
    private void checkCustomShortcutConflicts(CallbackInfo ci) {
        // Check if this keybinding conflicts with our custom shortcuts
        if (hasCustomShortcutConflict()) {
            this.duplicate = true;
        }
    }

    private boolean hasCustomShortcutConflict() {
        int keyCode = binding.getDefaultKey().getCode();

        // Check if any of our enabled shortcuts use this key
        for (Shortcut shortcut : PersistentData.get().shortcuts) {
            if (shortcut.enabled && shortcut.keyCode != 0) {
                // For keyboard keys, direct comparison
                if (keyCode == shortcut.keyCode) {
                    return true;
                }

                if (shortcut.keyCode >= 1000) {
                    int mouseButton = shortcut.keyCode - 1000;
                    if (keyCode == -(mouseButton + 1)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}