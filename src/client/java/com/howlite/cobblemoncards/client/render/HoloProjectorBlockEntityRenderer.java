package com.howlite.cobblemoncards.client.render;

import com.howlite.cobblemoncards.block.entity.HoloProjectorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

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

        long time = blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() : 0;
        int mode = blockEntity.getDisplayMode();
        
        // Mode 3 : Statique absolue. Pas de flottaison.
        if (mode == 3) {
            poseStack.translate(0.5, 1.4, 0.5); // Hauteur fixe
        }
        // Mode 4 : Posée horizontalement sur le projecteur
        else if (mode == 4) {
            poseStack.translate(0.5, 0.28, 0.5); // Juste au dessus du socle, fixe
        } 
        // Les autres modes (0, 1, 2, 5) utilisent le bobbing (flottaison)
        else {
            float offset = (float) Math.sin((time + partialTick) / 10.0f) * 0.1f;
            poseStack.translate(0.5, 1.4 + offset, 0.5);
        }

        Player localPlayer = Minecraft.getInstance().player;

        if (mode == 0) {
            // Mode 0 : Rotation Continue
            float rotation = (time + partialTick) * 3.0f;
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        } 
        else if (mode == 1 || mode == 2) {
            // Mode 1 : Toujours face au joueur
            // Mode 2 : Dynamique
            boolean facePlayer = (mode == 1);
            if (mode == 2 && localPlayer != null) {
                Vec3 blockPos = Vec3.atCenterOf(blockEntity.getBlockPos());
                double distance = localPlayer.position().distanceTo(blockPos);
                if (distance <= 5.0) {
                    facePlayer = true;
                }
            }

            if (facePlayer && localPlayer != null) {
                Vec3 blockPos = Vec3.atCenterOf(blockEntity.getBlockPos());
                double dX = localPlayer.getX() - blockPos.x;
                double dZ = localPlayer.getZ() - blockPos.z;
                float yaw = (float) (Math.atan2(dZ, dX) * (180D / Math.PI)) - 270.0f;
                poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
            } else if (mode == 2) {
                float rotation = (time + partialTick) * 3.0f;
                poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            }
        }
        else if (mode == 4) {
            // Mode 4 : Allongée / Horizontale
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0f)); // Coucher la carte
            // Rotation très lente pour l'esthétique
            float rotation = (time + partialTick) * 0.5f;
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));
        }
        // Mode 3 : Statique, Mode 5 : Juste le Bobbing de haut en bas sans rotation

        // Échelle (Scale)
        poseStack.scale(1.5f, 1.5f, 1.5f);

        // Lumière d'Hologramme (Glow)
        int light = LightTexture.FULL_BRIGHT;

        this.itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, light, packedOverlay, poseStack, bufferSource, blockEntity.getLevel(), 0);

        poseStack.popPose();
    }
}
