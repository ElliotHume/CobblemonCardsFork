package com.howlite.cobblemoncards.screen;

import com.howlite.cobblemoncards.menu.CardRecyclerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CardRecyclerScreen extends AbstractContainerScreen<CardRecyclerMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "textures/gui/card_recycler.png");

    public CardRecyclerScreen(CardRecyclerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        
        renderProgressArrow(graphics, x, y);
    }

    private void renderProgressArrow(GuiGraphics graphics, int x, int y) {
        int progress = menu.getScaledProgress();
        if (progress > 0) {
            // Position de la flèche sur l'UI personnalisée : X=85, Y=35 (ajustement selon grille 4x3)
            // Coordonnées de la flèche pleine dans le fichier : U=176, V=0, Largeur=24, Hauteur=17
            graphics.blit(TEXTURE, x + 85, y + 35, 176, 0, progress, 17);
        }
    }
}
