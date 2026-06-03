package com.howlite.cobblemoncards.network;

import com.howlite.cobblemoncards.CobblemonCards;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Payload S2C (Serveur -> Client) pour déclencher l'ouverture du CardShowcaseScreen.
 * Transporte une liste de CardItem stacks à afficher en mise en scène.
 */
public record RenderCardPayload(List<ItemStack> cards) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RenderCardPayload> ID =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, "render_card"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RenderCardPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.collection(java.util.ArrayList::new, ItemStack.STREAM_CODEC),
                    RenderCardPayload::cards,
                    RenderCardPayload::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
