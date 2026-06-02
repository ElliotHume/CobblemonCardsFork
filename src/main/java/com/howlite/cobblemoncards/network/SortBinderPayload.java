package com.howlite.cobblemoncards.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SortBinderPayload(int sortMode) implements CustomPacketPayload {
    public static final Type<SortBinderPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "sort_binder"));

    public static final StreamCodec<FriendlyByteBuf, SortBinderPayload> CODEC = CustomPacketPayload.codec(
            SortBinderPayload::write, SortBinderPayload::new);

    public SortBinderPayload(FriendlyByteBuf buf) {
        this(buf.readInt());
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeInt(sortMode);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
