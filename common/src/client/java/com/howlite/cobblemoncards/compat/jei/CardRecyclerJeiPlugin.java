package com.howlite.cobblemoncards.compat.jei;

import com.howlite.cobblemoncards.CobblemonCards;
import com.howlite.cobblemoncards.block.ModBlocks;
import com.howlite.cobblemoncards.compat.RecyclerRecipeInfo;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Point d'entrée JEI pour le mod Cobblemon Cards.
 * <p>
 * Chargé automatiquement par JEI via l'entrypoint {@code "jei_mod_plugin"} dans fabric.mod.json.
 * Si JEI n'est pas installé, cette classe n'est jamais chargée → aucun crash.
 */
@JeiPlugin
public class CardRecyclerJeiPlugin implements IModPlugin {

    /** Type de recette JEI pour le Card Recycler — référencé dans la Category */
    public static final RecipeType<RecyclerRecipeInfo> RECYCLER_TYPE = RecipeType.create(
            CobblemonCards.MOD_ID,
            "card_recycler",
            RecyclerRecipeInfo.class
    );

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new CardRecyclerJeiCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Enregistrer les 12 recettes d'exemple (6 raretés × normal/shiny)
        registration.addRecipes(RECYCLER_TYPE, RecyclerRecipeInfo.buildAll());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // Le bloc Card Recycler apparaît comme catalyseur de ces recettes
        // (l'utilisateur peut cliquer dessus pour voir les recettes associées)
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.CARD_RECYCLER),
                RECYCLER_TYPE
        );
    }
}
