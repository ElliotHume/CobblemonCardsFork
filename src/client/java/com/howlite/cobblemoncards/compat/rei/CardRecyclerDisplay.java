package com.howlite.cobblemoncards.compat.rei;

import com.howlite.cobblemoncards.compat.RecyclerRecipeInfo;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;

import java.util.Collections;
import java.util.List;

/**
 * Représente une recette individuelle du Card Recycler dans REI.
 * Encapsule l'entrée et la sortie sous forme d'EntryIngredient.
 */
public class CardRecyclerDisplay implements Display {

    private final List<EntryIngredient> inputs;
    private final List<EntryIngredient> outputs;
    private final RecyclerRecipeInfo info;

    public CardRecyclerDisplay(RecyclerRecipeInfo info) {
        this.info = info;
        this.inputs  = Collections.singletonList(EntryIngredient.of(EntryStacks.of(info.input)));
        this.outputs = Collections.singletonList(EntryIngredient.of(EntryStacks.of(info.output)));
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return inputs;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return outputs;
    }

    @Override
    public me.shedaniel.rei.api.common.category.CategoryIdentifier<?> getCategoryIdentifier() {
        return CardRecyclerReiCategory.CATEGORY_ID;
    }

    /** Accès au libellé pour l'affichage dans le renderer */
    public String getLabel() {
        return info.label;
    }
}
