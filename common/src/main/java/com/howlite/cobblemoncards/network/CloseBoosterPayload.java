package com.howlite.cobblemoncards.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CloseBoosterPayload() implements CustomPacketPayload {
    public static final Type<CloseBoosterPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "close_booster"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CloseBoosterPayload> CODEC = StreamCodec.unit(new CloseBoosterPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}