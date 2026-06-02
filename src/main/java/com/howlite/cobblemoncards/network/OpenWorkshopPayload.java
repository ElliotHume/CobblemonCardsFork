package com.howlite.cobblemoncards.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenWorkshopPayload() implements CustomPacketPayload {
    public static final Type<OpenWorkshopPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "open_workshop"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenWorkshopPayload> CODEC = StreamCodec.unit(new OpenWorkshopPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
