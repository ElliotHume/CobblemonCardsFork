package com.howlite.cobblemoncards.network;

import com.howlite.cobblemoncards.CobblemonCards;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ToggleProjectorNamePayload() implements CustomPacketPayload {
    public static final Type<ToggleProjectorNamePayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, "toggle_projector_name"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleProjectorNamePayload> CODEC = StreamCodec.unit(new ToggleProjectorNamePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
