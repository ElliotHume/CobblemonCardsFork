package com.howlite.cobblemoncards.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenBoosterPayload() implements CustomPacketPayload {
    // L'identifiant unique de notre message
    public static final Type<OpenBoosterPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "open_booster"));
    // Le traducteur pour transformer le message en données informatiques
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenBoosterPayload> CODEC = StreamCodec.unit(new OpenBoosterPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}