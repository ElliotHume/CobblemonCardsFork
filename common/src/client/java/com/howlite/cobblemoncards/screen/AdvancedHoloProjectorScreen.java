package com.howlite.cobblemoncards.screen;

import com.howlite.cobblemoncards.menu.AdvancedHoloProjectorMenu;
import com.howlite.cobblemoncards.network.ToggleProjectorNamePayload;
import com.mojang.blaze3d.systems.RenderSystem;
import com.howlite.cobblemoncards.util.PlatformHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedHoloProjectorScreen extends AbstractContainerScreen<AdvancedHoloProjectorMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");
    private Button toggleNameButton;

    public AdvancedHoloProjectorScreen(AdvancedHoloProjectorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 114 + 3 * 18; // Taille pour 3 rangées + inventaire joueur
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Bouton beaucoup plus petit et placé de manière compacte en haut à droite
        this.toggleNameButton = Button.builder(Component.translatable("gui.cobblemon-cards.advanced_holo_projector.toggle_name"), button -> {
            PlatformHelper.INSTANCE.sendToServer(new ToggleProjectorNamePayload());
        })
        .bounds(x + this.imageWidth - 85, y - 15, 78, 12)
        .build();

        this.addRenderableWidget(this.toggleNameButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, i, j, 0, 0, this.imageWidth, 3 * 18 + 17);
        guiGraphics.blit(TEXTURE, i, j + 3 * 18 + 17, 0, 126, this.imageWidth, 96);
    }
}
