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

        System.out.println("****************************************************************");
        System.out.println("[CobblemonCards MIXIN] Called. Target: " + result.getItem() + ", isTarget: " + isTarget);
        System.out.println("****************************************************************");

        if (isTarget) {
            for (int i = 0; i < craftingInput.size(); i++) {
                ItemStack ingredient = craftingInput.getItem(i);
                if (ingredient.isEmpty()) continue;
                
                boolean isSource = ingredient.getItem() instanceof BinderItem;
                System.out.println("[CobblemonCards MIXIN] Slot " + i + ": " + ingredient.getItem() + ", isSource: " + isSource);

                if (isSource) {
                    // 1. Si la source a le composant BINDER_CONTENTS
                    if (ingredient.has(com.howlite.cobblemoncards.component.ModDataComponents.BINDER_CONTENTS)) {
                        java.util.List<ItemStack> contents = ingredient.get(com.howlite.cobblemoncards.component.ModDataComponents.BINDER_CONTENTS);
                        System.out.println("[CobblemonCards MIXIN] Source has BINDER_CONTENTS. Size: " + (contents != null ? contents.size() : "null"));
                        if (contents != null) {
                            if (result.getItem() instanceof BinderItem) {
                                result.set(com.howlite.cobblemoncards.component.ModDataComponents.BINDER_CONTENTS, contents);
                                System.out.println("[CobblemonCards MIXIN] Copied BINDER_CONTENTS to target binder.");
                            } else if (result.is(ModBlocks.CARD_CABINET.asItem())) {
                                result.set(DataComponents.CONTAINER, net.minecraft.world.item.component.ItemContainerContents.fromItems(contents));
                                System.out.println("[CobblemonCards MIXIN] Converted BINDER_CONTENTS to CONTAINER for target cabinet.");
                            }
                        }
                    }
                    // 2. Sinon, fallback legacy si la source utilise encore CONTAINER
                    else if (ingredient.has(DataComponents.CONTAINER)) {
                        net.minecraft.world.item.component.ItemContainerContents contents = ingredient.get(DataComponents.CONTAINER);
                        System.out.println("[CobblemonCards MIXIN] Source has legacy CONTAINER.");
                        if (contents != null) {
                            if (result.getItem() instanceof BinderItem) {
                                int totalSlots = (ingredient.getItem() instanceof BinderItem binder) ? binder.getTier().getMaxSlots(12) : 256;
                                net.minecraft.core.NonNullList<ItemStack> legacy = net.minecraft.core.NonNullList.withSize(Math.min(totalSlots, 256), ItemStack.EMPTY);
                                contents.copyInto(legacy);
                                result.set(com.howlite.cobblemoncards.component.ModDataComponents.BINDER_CONTENTS, java.util.Collections.unmodifiableList(legacy));
                                System.out.println("[CobblemonCards MIXIN] Converted legacy CONTAINER to BINDER_CONTENTS for target binder.");
                            } else if (result.is(ModBlocks.CARD_CABINET.asItem())) {
                                result.set(DataComponents.CONTAINER, contents);
                                System.out.println("[CobblemonCards MIXIN] Copied legacy CONTAINER to target cabinet.");
                            }
                        }
                    } else {
                        System.out.println("[CobblemonCards MIXIN] Source has NO card contents components.");
                    }
                    break;
                }
            }
        }
    }
}
