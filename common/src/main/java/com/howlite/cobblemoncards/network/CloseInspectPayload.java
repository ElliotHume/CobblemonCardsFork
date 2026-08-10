package com.howlite.cobblemoncards.network;

import com.howlite.cobblemoncards.CobblemonCards;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Payload C2S (Client → Server) envoyé quand le joueur ferme son CardInspectScreen.
 */
public record CloseInspectPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CloseInspectPayload> ID =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, "close_inspect"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CloseInspectPayload> CODEC =
            StreamCodec.unit(new CloseInspectPayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
