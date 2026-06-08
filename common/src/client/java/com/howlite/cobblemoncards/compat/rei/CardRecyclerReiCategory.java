package com.howlite.cobblemoncards.compat.rei;

import com.howlite.cobblemoncards.CobblemonCards;
import com.howlite.cobblemoncards.block.ModBlocks;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Catégorie REI pour le Card Recycler.
 * Implémentation simplifiée avec uniquement slots + arrow (éléments stables de l'API 16.x).
 */
public class CardRecyclerReiCategory implements DisplayCategory<CardRecyclerDisplay> {

    public static final CategoryIdentifier<CardRecyclerDisplay> CATEGORY_ID =
            CategoryIdentifier.of(CobblemonCards.MOD_ID, "card_recycler");

    @Override
    public CategoryIdentifier<CardRecyclerDisplay> getCategoryIdentifier() {
        return CATEGORY_ID;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.cobblemon-cards.card_recycler");
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(new ItemStack(ModBlocks.CARD_RECYCLER));
    }

    /**
     * Largeur de la fenêtre de recette.
     * On laisse la valeur par défaut (overridée pour certaines versions de REI).
     */
    @Override
    public int getDisplayWidth(CardRecyclerDisplay display) {
        return 150;
    }

    @Override
    public int getDisplayHeight() {
        return 36;
    }

    @Override
    public List<Widget> setupDisplay(CardRecyclerDisplay display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();

        // Ancre de départ (coin supérieur gauche de la zone de recette)
        int x = bounds.x;
        int y = bounds.y;

        // Base de la recette (arrière-plan et bordures)
        widgets.add(Widgets.createRecipeBase(bounds));

        // Slot d'ENTRÉE (carte) — à gauche
        widgets.add(
            Widgets.createSlot(new Point(x + 10, y + 9))
                   .entries(display.getInputEntries().get(0))
                   .markInput()
        );

        // Flèche → au centre (24×17 px, centrée verticalement)
        widgets.add(Widgets.createArrow(new Point(x + 40, y + 10)));

        // Slot de SORTIE (Card Dust) — à droite
        widgets.add(
            Widgets.createSlot(new Point(x + 76, y + 9))
                   .entries(display.getOutputEntries().get(0))
                   .markOutput()
        );

        return widgets;
    }
}
