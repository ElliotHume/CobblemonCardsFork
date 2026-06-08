package com.howlite.cobblemoncards.render;

import com.howlite.cobblemoncards.block.entity.HoloProjectorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class HoloProjectorBlockEntityRenderer implements BlockEntityRenderer<HoloProjectorBlockEntity> {
    private final ItemRenderer itemRenderer;

    public HoloProjectorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(HoloProjectorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack itemStack = blockEntity.getItem(0);
        if (itemStack.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        // Flottaison
        long time = blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() : 0;
        float offset = (float) Math.sin((time + partialTick) / 10.0f) * 0.1f;
        poseStack.translate(0.5, 1.2 + offset, 0.5);

        // Rotation
        float rotation = (time + partialTick) * 3.0f; // 3 degrees per tick
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        // Échelle
        poseStack.scale(2.0f, 2.0f, 2.0f);

        // Lumière (Glow)
        int light = LightTexture.FULL_BRIGHT;

        this.itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, light, packedOverlay, poseStack, bufferSource, blockEntity.getLevel(), 0);

        poseStack.popPose();

        // Avec Iris+Sodium (sans custom shaders GLSL), le fallback CPU écrit dans des
        // RenderTypes custom (entityTranslucent, entityCutout) via le MultiBufferSource.
        // Dans un BlockEntityRenderer, ces buffers ne sont jamais automatiquement flushés
        // par le pipeline standard. On force le flush ici pour que la géométrie soit soumise au GPU.
        if (bufferSource instanceof MultiBufferSource.BufferSource immediateSource) {
            immediateSource.endBatch();
        }
    }
}

