package com.howlite.cobblemoncards.mixin;

import com.howlite.cobblemoncards.item.custom.BinderItem;
import com.howlite.cobblemoncards.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapedRecipe.class)
public class ShapedRecipeMixin {
    @Inject(method = "assemble", at = @At("RETURN"), cancellable = true)
    private void copyBinderContainer(CraftingInput craftingInput, HolderLookup.Provider provider, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        boolean isTarget = result.getItem() instanceof BinderItem || result.is(ModBlocks.CARD_CABINET.asItem());

        if (isTarget) {
            for (int i = 0; i < craftingInput.size(); i++) {
                ItemStack ingredient = craftingInput.getItem(i);
                boolean isSource = ingredient.getItem() instanceof BinderItem;

                if (isSource && ingredient.has(DataComponents.CONTAINER)) {
                    result.set(DataComponents.CONTAINER, ingredient.get(DataComponents.CONTAINER));
                    break;
                }
            }
        }
    }
}
