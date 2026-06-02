package com.howlite.cobblemoncards.client.render;

import com.howlite.cobblemoncards.block.entity.GradingStationBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class GradingStationBlockEntityRenderer implements BlockEntityRenderer<GradingStationBlockEntity> {

    public GradingStationBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(GradingStationBlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        ItemStack stack = blockEntity.getItem(0);
        if (!stack.isEmpty()) {
            poseStack.pushPose();

            // Position au centre du bloc (X=0.5, Z=0.5) et un peu plus bas qu'avant (Y=0.45)
            // L'offset de flottement est conservé
            double offset = Math.sin((blockEntity.getLevel().getGameTime() + partialTicks) / 8.0) * 0.1;
            poseStack.translate(0.5, 0.45 + offset, 0.5);

            // Rotation continue
            float rotation = (blockEntity.getLevel().getGameTime() + partialTicks) * 2.0f;
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

            // Taille de la carte (agrandie de 0.5 à 0.75)
            poseStack.scale(0.75f, 0.75f, 0.75f);

            // Rendu de l'item
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, combinedLight, combinedOverlay, poseStack, buffer, blockEntity.getLevel(), 0);

            poseStack.popPose();
        }
    }
}