package com.howlite.cobblemoncards.compat.jei;

import com.howlite.cobblemoncards.CobblemonCards;
import com.howlite.cobblemoncards.block.ModBlocks;
import com.howlite.cobblemoncards.compat.RecyclerRecipeInfo;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Catégorie JEI pour le Card Recycler.
 * Affiche un slot d'entrée (carte) → flèche → slot de sortie (Card Dust).
 */
public class CardRecyclerJeiCategory implements IRecipeCategory<RecyclerRecipeInfo> {

    public static final ResourceLocation CATEGORY_UID =
            ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, "card_recycler");

    // Dimensions de la "fenêtre" de recette dans JEI (en pixels)
    private static final int WIDTH  = 116;
    private static final int HEIGHT = 54;

    // Positions des slots dans la fenêtre
    private static final int INPUT_X  = 14;
    private static final int INPUT_Y  = 18;
    private static final int OUTPUT_X = 82;
    private static final int OUTPUT_Y = 18;
    private static final int ARROW_X  = 42;
    private static final int ARROW_Y  = 19;

    private final IDrawable icon;
    private final IDrawable arrow;

    public CardRecyclerJeiCategory(IGuiHelper guiHelper) {
        // Icône = le bloc Card Recycler
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.CARD_RECYCLER));
        // Flèche de progression issue de la texture vanilla de fourneau
        this.arrow = guiHelper.drawableBuilder(
                ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png"),
                79, 34, 24, 17
        ).build();
    }

    @Override
    public mezz.jei.api.recipe.RecipeType<RecyclerRecipeInfo> getRecipeType() {
        return CardRecyclerJeiPlugin.RECYCLER_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.cobblemon-cards.card_recycler");
    }

    /** Largeur de la fenêtre de recette (remplacement de getBackground() déprécié) */
    @Override
    public int getWidth() {
        return WIDTH;
    }

    /** Hauteur de la fenêtre de recette (remplacement de getBackground() déprécié) */
    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecyclerRecipeInfo recipe, IFocusGroup focuses) {
        // Slot d'entrée (la carte)
        IRecipeSlotBuilder inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X, INPUT_Y);
        inputSlot.addItemStack(recipe.input);

        // Slot de sortie (Card Dust)
        IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, OUTPUT_Y);
        outputSlot.addItemStack(recipe.output);
    }

    @Override
    public void draw(RecyclerRecipeInfo recipe,
                     mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView,
                     net.minecraft.client.gui.GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        // Dessiner la flèche
        arrow.draw(guiGraphics, ARROW_X, ARROW_Y);

        // Afficher le label de la recette sous les slots
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        guiGraphics.drawString(
                mc.font,
                recipe.label,
                0, HEIGHT - 10,
                0x555555,
                false
        );
    }
}
