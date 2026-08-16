package com.howlite.cobblemoncards.network;

import com.howlite.cobblemoncards.CobblemonCards;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Payload S2C (Serveur → Client) pour ouvrir le CardInspectScreen.
 * Transporte un seul ItemStack de carte à inspecter.
 */
public record InspectCardPayload(ItemStack card) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<InspectCardPayload> ID =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, "inspect_card"));

    public static final StreamCodec<RegistryFriendlyByteBuf, InspectCardPayload> CODEC =
            StreamCodec.composite(
                    ItemStack.STREAM_CODEC,
                    InspectCardPayload::card,
                    InspectCardPayload::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
