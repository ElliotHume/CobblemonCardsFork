package com.howlite.cobblemoncards.client.render;

import com.howlite.cobblemoncards.block.entity.CardCabinetBlockEntity;
import com.howlite.cobblemoncards.item.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CardCabinetBlockEntityRenderer implements BlockEntityRenderer<CardCabinetBlockEntity> {
    private final ItemRenderer itemRenderer;
    private static final ItemStack MASTER_ALBUM_STACK = new ItemStack(ModItems.MASTER_ALBUM);

    public CardCabinetBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(CardCabinetBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        // 1. Déplacer vers le centre du bloc
        poseStack.translate(0.5, 0.0, 0.5);

        // 2. Orienter le livre selon la rotation (FACING) du bloc Card Cabinet
        net.minecraft.world.level.block.state.BlockState state = blockEntity.getBlockState();
        if (state.hasProperty(com.howlite.cobblemoncards.block.CardCabinetBlock.FACING)) {
            net.minecraft.core.Direction facing = state
                    .getValue(com.howlite.cobblemoncards.block.CardCabinetBlock.FACING);
            // Aligne la rotation sur le facing du bloc
            poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        }

        // 3. Aller à l'origine de la rotation du plan incliné du modèle (Y = 13/16, Z =
        // -0.5)
        poseStack.translate(0.0, 0.8125, -0.5);

        // 4. Appliquer la rotation de -22.5 degrés sur l'axe X local pour épouser la
        // pente
        poseStack.mulPose(Axis.XP.rotationDegrees(22.5f));

        // 5. Positionner le livre sur le plan incliné :
        // - zOffset : position le long du plan incliné (0.65 le centre parfaitement sur
        // la pente)
        // - yOffset : hauteur par rapport au plan incliné (0.08 pour éviter la
        // lévitation et le Z-fighting)
        double zOffset = 0.4;
        double yOffset = 0.5;
        poseStack.translate(0.0, yOffset, zOffset);

        // 6. Échelle de l'album (augmentée pour être plus visible et imposante)
        poseStack.scale(1.3f, 1.3f, 1.3f);

        // 7. Récupération de la luminosité du bloc au-dessus
        int light = packedLight;
        if (blockEntity.getLevel() != null) {
            light = LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos().above());
        }

        // 8. Rendu de l'ItemStack sous forme d'item plat au sol (GROUND)
        this.itemRenderer.renderStatic(MASTER_ALBUM_STACK, ItemDisplayContext.GROUND, light, packedOverlay, poseStack,
                bufferSource, blockEntity.getLevel(), 0);

        poseStack.popPose();
    }
}
