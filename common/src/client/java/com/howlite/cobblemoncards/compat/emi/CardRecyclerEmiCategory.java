package com.howlite.cobblemoncards.compat.emi;

import com.howlite.cobblemoncards.CobblemonCards;
import com.howlite.cobblemoncards.block.ModBlocks;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Catégorie EMI pour le Card Recycler.
 * Définit l'icône (le bloc Card Recycler) et l'identifiant de la catégorie.
 */
public class CardRecyclerEmiCategory extends EmiRecipeCategory {

    public static final ResourceLocation CATEGORY_ID =
            ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, "card_recycler");

    public CardRecyclerEmiCategory() {
        super(
                CATEGORY_ID,
                // Icône : le bloc Card Recycler sous forme d'EmiStack
                EmiStack.of(new ItemStack(ModBlocks.CARD_RECYCLER))
        );
    }

    /**
     * Nom affiché dans l'interface EMI.
     * Évite le warning "Untranslated recipe category cobblemon-cards:card_recycler".
     * Réutilise la clé de traduction déjà existante du bloc.
     */
    @Override
    public net.minecraft.network.chat.Component getName() {
        return net.minecraft.network.chat.Component.translatable("block.cobblemon-cards.card_recycler");
    }
}
