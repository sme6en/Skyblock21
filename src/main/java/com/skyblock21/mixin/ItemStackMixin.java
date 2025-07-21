package com.skyblock21.mixin;

import com.skyblock21.config.persistent.PersistentData;
import com.skyblock21.features.itemcustomization.ItemCustomization;
import com.skyblock21.features.items.CompactStars;
import com.skyblock21.util.TextUtils;
import com.skyblock21.util.Utils;
import net.minecraft.component.ComponentHolder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ComponentHolder {

    String lastName;

    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract boolean isEmpty();

    @ModifyVariable(method = "getName", at = @At("STORE"))
    private Text modifyItemName(Text name) {
        if (name == null || name.getString().isEmpty()) {
            return name;
        }

        ItemStack self = (ItemStack) (Object) this;
        String uuid = Utils.getItemUUID(self);
        Text finalName = name;

        if (!uuid.isEmpty()) {
            ItemCustomization customization = PersistentData.get().itemCustomizations.get(uuid);
            if (customization != null && customization.hasCustomName()) {
                String customName = TextUtils.translateColorCodes(customization.customName, false);
                finalName = Text.literal(customName);
            }
        }

        if (lastName == null || !lastName.equals(finalName.getString())) {
            lastName = finalName.getString();
        }

        return CompactStars.modifyText(finalName);
    }

    @Inject(method = "hasGlint", at = @At("RETURN"), cancellable = true)
    private void hasCustomGlint(CallbackInfoReturnable<Boolean> cir) {
        if (this.isEmpty()) return;

        ItemStack self = (ItemStack) (Object) this;
        String uuid = Utils.getItemUUID(self);

        if (!uuid.isEmpty()) {
            ItemCustomization customization = PersistentData.get().itemCustomizations.get(uuid);
            if (customization != null) {
                cir.setReturnValue(customization.hasGlint);
            }
        }
    }
}