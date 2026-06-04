package com.howlite.cobblemoncards.client.render;

import com.howlite.cobblemoncards.block.entity.AdvancedHoloProjectorBlockEntity;
import com.howlite.cobblemoncards.component.CardData;
import com.howlite.cobblemoncards.component.ModDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class AdvancedHoloProjectorBlockEntityRenderer implements BlockEntityRenderer<AdvancedHoloProjectorBlockEntity> {
    private final ItemRenderer itemRenderer;
    private final Font font;

    public AdvancedHoloProjectorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
        this.font = context.getFont();
    }

    @Override
    public boolean shouldRenderOffScreen(AdvancedHoloProjectorBlockEntity blockEntity) {
        return true; // Force le rendu même si la base du bloc n'est pas à l'écran
    }

    @Override
    public void render(AdvancedHoloProjectorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        long time = blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() : 0;
        int mode = blockEntity.getDisplayMode();
        Player localPlayer = Minecraft.getInstance().player;

        List<ItemStack> cardsToDisplay = new ArrayList<>();
        for (int i = 0; i < blockEntity.getContainerSize(); i++) {
            if (!blockEntity.getItem(i).isEmpty()) {
                cardsToDisplay.add(blockEntity.getItem(i));
            }
        }

        if (cardsToDisplay.isEmpty()) return;

        int displayDuration = 60; 
        int currentIndex = (int) ((time / displayDuration) % cardsToDisplay.size());
        ItemStack itemStack = cardsToDisplay.get(currentIndex);

        // --- PARTIE 1 : RENDU DE LA CARTE ---
        poseStack.pushPose();

        if (mode == 3) {
            poseStack.translate(0.5, 1.4, 0.5); 
        } else if (mode == 4) {
            poseStack.translate(0.5, 0.28, 0.5); 
        } else {
            float offset = (float) Math.sin((time + partialTick) / 10.0f) * 0.1f;
            poseStack.translate(0.5, 1.4 + offset, 0.5);
        }

        if (mode == 0) {
            float rotation = (time + partialTick) * 3.0f;
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        } 
        else if (mode == 1 || mode == 2) {
            boolean facePlayer = (mode == 1);
            if (mode == 2 && localPlayer != null) {
                Vec3 blockPos = Vec3.atCenterOf(blockEntity.getBlockPos());
                double distance = localPlayer.position().distanceTo(blockPos);
                if (distance <= 5.0) facePlayer = true;
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
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0f)); 
            float rotation = (time + partialTick) * 0.5f;
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));
        }

        poseStack.scale(1.5f, 1.5f, 1.5f);

        int light = LightTexture.FULL_BRIGHT;
        this.itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, light, packedOverlay, poseStack, bufferSource, blockEntity.getLevel(), 0);

        poseStack.popPose();

        // --- PARTIE 2 : RENDU DU NOM ---
        if (blockEntity.isShowName()) {
            Component text = itemStack.getHoverName();
            CardData data = itemStack.get(ModDataComponents.CARD_DATA);
            if (data != null && data.pokemonId() != null && !data.pokemonId().isEmpty()) {
                String pokemonName = data.pokemonId().substring(0, 1).toUpperCase() + data.pokemonId().substring(1);
                text = Component.literal(pokemonName);
            }

            poseStack.pushPose();
            
            // Hauteur
            float nameOffset = (mode == 4) ? 0.8f : 2.5f;
            if (mode != 3 && mode != 4) {
                nameOffset += (float) Math.sin((time + partialTick) / 10.0f) * 0.1f;
            }
            poseStack.translate(0.5, nameOffset, 0.5);

            // Retour à la méthode de la caméra qui marchait parfaitement pour s'orienter !
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
            poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));

            poseStack.scale(-0.025f, -0.025f, 0.025f);
            Matrix4f matrix4f = poseStack.last().pose();
            
            float textWidth = this.font.width(text);
            float xOffset = -textWidth / 2.0f;
            
            // Rendu See Through
            int backgroundColor = Minecraft.getInstance().options.getBackgroundColor(0.25f);
            this.font.drawInBatch(text, xOffset, 0, 553648127, false, matrix4f, bufferSource, Font.DisplayMode.SEE_THROUGH, backgroundColor, LightTexture.FULL_BRIGHT);
            this.font.drawInBatch(text, xOffset, 0, -1, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);

            poseStack.popPose();
        }

        if (bufferSource instanceof MultiBufferSource.BufferSource immediateSource) {
            immediateSource.endBatch();
        }
    }
}
