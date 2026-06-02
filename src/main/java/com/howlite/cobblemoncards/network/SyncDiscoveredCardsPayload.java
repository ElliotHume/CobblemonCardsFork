package com.howlite.cobblemoncards.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

public record SyncDiscoveredCardsPayload(List<String> discoveredIds) implements CustomPacketPayload {

    public static final Type<SyncDiscoveredCardsPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "sync_discovered_cards"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncDiscoveredCardsPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
            SyncDiscoveredCardsPayload::discoveredIds,
            SyncDiscoveredCardsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
