package com.howlite.cobblemoncards.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BinderPagePayload(int page) implements CustomPacketPayload {
    public static final Type<BinderPagePayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "binder_page"));

    public static final StreamCodec<FriendlyByteBuf, BinderPagePayload> CODEC = CustomPacketPayload.codec(
            BinderPagePayload::write, BinderPagePayload::new);

    public BinderPagePayload(FriendlyByteBuf buf) {
        this(buf.readInt());
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeInt(page);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}