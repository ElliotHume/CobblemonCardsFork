package com.howlite.cobblemoncards.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GenerateCardPayload(
        String pokemonId,
        boolean isShiny,
        String rarity,
        String statName,
        float statValue,
        String background,
        String effect
) implements CustomPacketPayload {
    public static final Type<GenerateCardPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "generate_card"));

    public static final StreamCodec<FriendlyByteBuf, GenerateCardPayload> CODEC = CustomPacketPayload.codec(
            GenerateCardPayload::write, GenerateCardPayload::new);

    public GenerateCardPayload(FriendlyByteBuf buf) {
        this(buf.readUtf(), buf.readBoolean(), buf.readUtf(), buf.readUtf(), buf.readFloat(), buf.readUtf(), buf.readUtf());
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeUtf(pokemonId);
        buf.writeBoolean(isShiny);
        buf.writeUtf(rarity);
        buf.writeUtf(statName);
        buf.writeFloat(statValue);
        buf.writeUtf(background);
        buf.writeUtf(effect);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
