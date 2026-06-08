package com.howlite.cobblemoncards.compat;

import com.howlite.cobblemoncards.component.CardData;
import com.howlite.cobblemoncards.component.CardStat;
import com.howlite.cobblemoncards.component.ModDataComponents;
import com.howlite.cobblemoncards.item.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Représente une recette d'exemple du Card Recycler pour les interfaces de recipe viewers.
 * <p>
 * Une entrée par combinaison rareté × shiny (12 au total).
 * Les montants de Card Dust sont calculés selon la même logique que CardRecyclerBlockEntity.
 */
public class RecyclerRecipeInfo {

    /** Slot d'entrée : une carte avec les CardData correspondantes */
    public final ItemStack input;

    /** Slot de sortie : Card Dust × N */
    public final ItemStack output;

    /** Libellé affiché : ex. "Common Card → 1 Card Dust" */
    public final String label;

    /** Identifiant unique pour le recipe viewer (ex. "common_normal") */
    public final String id;

    public RecyclerRecipeInfo(ItemStack input, ItemStack output, String label, String id) {
        this.input  = input;
        this.output = output;
        this.label  = label;
        this.id     = id;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Données statiques
    // ──────────────────────────────────────────────────────────────────────────

    /** Toutes les raretés supportées dans l'ordre d'affichage. */
    private static final String[] RARITIES = {
            "common", "uncommon", "rare", "epic", "legendary", "mythic"
    };

    /**
     * Construit la liste complète des recettes d'exemple (12 entrées : 6 raretés × normal/shiny).
     * Appelé une seule fois au chargement du plugin.
     */
    public static List<RecyclerRecipeInfo> buildAll() {
        List<RecyclerRecipeInfo> recipes = new ArrayList<>();

        for (String rarity : RARITIES) {
            recipes.add(build(rarity, false));
            recipes.add(build(rarity, true));
        }

        return recipes;
    }

    /**
     * Construit une entrée pour une rareté et un état shiny donnés.
     * Le montant de poudre est calculé avec la logique de base (sans background ni holo
     * pour avoir le minimum garanti, car ces bonus dépendent du pokemonId).
     *
     * <p>Logique identique à {@code CardRecyclerBlockEntity#calculateDustAmount} :
     * <ul>
     *   <li>common → 1, uncommon → 2, rare → 3, epic → 5, legendary → 10, mythic → 15</li>
     *   <li>shiny → × 2</li>
     * </ul>
     */
    private static RecyclerRecipeInfo build(String rarity, boolean shiny) {
        int base = switch (rarity.toLowerCase()) {
            case "common"    -> 1;
            case "uncommon"  -> 2;
            case "rare"      -> 3;
            case "epic"      -> 5;
            case "legendary" -> 10;
            case "mythic"    -> 15;
            default          -> 1;
        };
        int dustAmount = shiny ? base * 2 : base;

        // Construire l'ItemStack d'entrée avec les CardData minimales
        CardData data = new CardData(
                "bulbasaur",           // pokemonId : visuellement représentatif
                shiny,
                rarity,
                CardStat.MOVEMENT_SPEED,
                1.0f,
                0,
                Optional.empty(),      // pas de background → pas de bonus dans l'exemple
                Optional.empty()       // pas d'effet holo → pas de bonus dans l'exemple
        );
        ItemStack inputStack = new ItemStack(ModItems.CARD);
        inputStack.set(ModDataComponents.CARD_DATA, data);

        // Construire l'ItemStack de sortie
        ItemStack outputStack = new ItemStack(ModItems.CARD_DUST, dustAmount);

        // Libellé
        String capitalize = rarity.substring(0, 1).toUpperCase() + rarity.substring(1);
        String shinyPrefix = shiny ? "Shiny " : "";
        String label = shinyPrefix + capitalize + " Card → " + dustAmount + " Card Dust";

        // ID unique
        String id = rarity + (shiny ? "_shiny" : "_normal");

        return new RecyclerRecipeInfo(inputStack, outputStack, label, id);
    }

    /**
     * Retourne un ResourceLocation unique pour cette recette,
     * utilisé comme identifiant dans JEI/EMI.
     *
     * Le préfixe '/' dans le path indique à EMI que cette recette est synthétique
     * (non présente dans le RecipeManager vanilla), ce qui supprime les warnings
     * "not present in recipe manager. Consider prefixing its path with '/'".
     */
    public ResourceLocation getRecipeId() {
        return ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "/recycler/" + id);
    }
}
