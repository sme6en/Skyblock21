package com.skyblock21.mixin;

import com.skyblock21.features.items.CompactStars;
import com.skyblock21.util.TextUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import net.minecraft.text.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    String lastName;

    @ModifyVariable(method = "getName", at = @At("STORE"))
    private Text modifyItemName(Text name) {
        if (name == null || name.getString().isEmpty()) {
            return name;
        }

        if (lastName == null || !lastName.equals(name.getString())) {
//            System.out.println("Modifying item name: " + TextUtils.toLegacy(name));
            lastName = name.getString();
        }
        return CompactStars.modifyText(name);
    }

}
