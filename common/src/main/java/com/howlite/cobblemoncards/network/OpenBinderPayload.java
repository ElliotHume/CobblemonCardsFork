package com.howlite.cobblemoncards.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenBinderPayload() implements CustomPacketPayload {
    public static final Type<OpenBinderPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "open_binder"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenBinderPayload> CODEC = StreamCodec.unit(new OpenBinderPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
