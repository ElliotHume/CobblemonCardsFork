package com.howlite.cobblemoncards.compat.emi;

import com.howlite.cobblemoncards.compat.RecyclerRecipeInfo;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Représente une recette individuelle du Card Recycler dans EMI.
 * Fournit les inputs, outputs et le rendu via WidgetHolder.
 */
public class CardRecyclerEmiRecipe implements EmiRecipe {

    // Flèche de fourneau vanilla pour le rendu (u=79, v=34, w=24, h=17)
    private static final ResourceLocation FURNACE_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png");

    private final RecyclerRecipeInfo info;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;

    public CardRecyclerEmiRecipe(RecyclerRecipeInfo info) {
        this.info    = info;
        // EmiStack implémente EmiIngredient directement — pas de wrapper nécessaire
        this.inputs  = List.of(EmiStack.of(info.input));
        this.outputs = List.of(EmiStack.of(info.output));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return CardRecyclerEmiPlugin.CATEGORY;
    }

    @Override
    public ResourceLocation getId() {
        return info.getRecipeId();
    }


    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    // Dimensions de l'écran de recette (en pixels)
    @Override
    public int getDisplayWidth() {
        return 116;
    }

    @Override
    public int getDisplayHeight() {
        return 54;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // Slot d'entrée (carte)
        widgets.addSlot(inputs.get(0), 14, 18);

        // Slot de sortie (Card Dust) avec indication de sortie
        widgets.addSlot(outputs.get(0), 82, 18).recipeContext(this);

        // Flèche vanilla (issue de la texture fourneau)
        widgets.addTexture(FURNACE_TEXTURE, 42, 19, 24, 17, 79, 34);

        // Label de la recette
        widgets.addText(
                net.minecraft.network.chat.Component.literal(info.label),
                58,  // x centré dans la fenêtre
                44,  // y en bas
                0x555555,
                false
        );
    }
}
