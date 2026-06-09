package com.howlite.cobblemoncards.neoforge.item;

import com.howlite.cobblemoncards.item.custom.CardItem;
import com.howlite.cobblemoncards.render.CardItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import java.util.function.Consumer;

@SuppressWarnings("null")
public class NeoForgeCardItem extends CardItem {
    public NeoForgeCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new BlockEntityWithoutLevelRenderer(
                            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                            Minecraft.getInstance().getEntityModels()
                    ) {
                        @Override
                        public void renderByItem(net.minecraft.world.item.ItemStack stack, net.minecraft.world.item.ItemDisplayContext displayContext, com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource buffer, int packedLight, int packedOverlay) {
                            CardItemRenderer.render(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
                        }
                    };
                }
                return renderer;
            }
        });
    }
}
