package com.howlite.cobblemoncards.client.render;

import com.howlite.cobblemoncards.block.MiniHoloProjectorBlock;
import com.howlite.cobblemoncards.block.entity.MiniHoloProjectorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class MiniHoloProjectorBlockEntityRenderer implements BlockEntityRenderer<MiniHoloProjectorBlockEntity> {
    private final ItemRenderer itemRenderer;

    public MiniHoloProjectorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(MiniHoloProjectorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (blockEntity.getLevel() == null) return;

        int projectorsCount = 1;
        if (blockEntity.getBlockState().hasProperty(MiniHoloProjectorBlock.PROJECTORS)) {
            projectorsCount = blockEntity.getBlockState().getValue(MiniHoloProjectorBlock.PROJECTORS);
        }

        long time = blockEntity.getLevel().getGameTime();
        Player localPlayer = Minecraft.getInstance().player;

        for (int slot = 0; slot < projectorsCount; slot++) {
            ItemStack itemStack = blockEntity.getItem(slot);
            if (itemStack.isEmpty()) continue;

            float subX, subZ;
            if (projectorsCount == 1) {
                subX = 0.5f; subZ = 0.5f;
            } else if (projectorsCount == 2) {
                subX = (slot == 0) ? 0.33f : 0.67f;
                subZ = 0.5f;
            } else if (projectorsCount == 3) {
                if (slot == 0)      { subX = 0.33f; subZ = 0.33f; }
                else if (slot == 1) { subX = 0.67f; subZ = 0.33f; }
                else                { subX = 0.50f; subZ = 0.67f; }
            } else {
                if (slot == 0)      { subX = 0.30f; subZ = 0.30f; } // NW
                else if (slot == 1) { subX = 0.70f; subZ = 0.30f; } // NE
                else if (slot == 2) { subX = 0.30f; subZ = 0.70f; } // SW
                else                { subX = 0.70f; subZ = 0.70f; } // SE
            }

            poseStack.pushPose();

            int mode = blockEntity.getDisplayMode(slot);

            if (mode == 3) {
                poseStack.translate(subX, 0.75, subZ);
            } else if (mode == 4) {
                poseStack.translate(subX, 0.15, subZ);
            } else {
                float bobbing = (float) Math.sin((time + partialTick + slot * 7) / 10.0f) * 0.05f;
                poseStack.translate(subX, 0.75 + bobbing, subZ);
            }

            if (mode == 0) {
                float rotation = (time + partialTick + slot * 15) * 3.0f;
                poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            } else if (mode == 1 || mode == 2) {
                boolean facePlayer = (mode == 1);
                if (mode == 2 && localPlayer != null) {
                    Vec3 center = Vec3.atCenterOf(blockEntity.getBlockPos());
                    if (localPlayer.position().distanceTo(center) <= 5.0) {
                        facePlayer = true;
                    }
                }
                if (facePlayer && localPlayer != null) {
                    Vec3 center = Vec3.atCenterOf(blockEntity.getBlockPos());
                    double dX = localPlayer.getX() - center.x;
                    double dZ = localPlayer.getZ() - center.z;
                    float yaw = (float) (Math.atan2(dZ, dX) * (180D / Math.PI)) - 270.0f;
                    poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
                } else if (mode == 2) {
                    float rotation = (time + partialTick + slot * 15) * 3.0f;
                    poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
                }
            } else if (mode == 4) {
                poseStack.mulPose(Axis.XP.rotationDegrees(90f));
            }

            // Scale to mini card proportions
            float sc = (projectorsCount > 1) ? 0.50f : 0.65f;
            poseStack.scale(sc, sc, sc);

            itemRenderer.renderStatic(
                    itemStack,
                    ItemDisplayContext.FIXED,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    bufferSource,
                    blockEntity.getLevel(),
                    0
            );

            poseStack.popPose();
        }
    }
}
