package com.howlite.cobblemoncards.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Payload C2S : le client envoie le grade cible souhaité pour le Card Restorer.
 */
public record ChangeRestorerTargetGradePayload(int targetGrade) implements CustomPacketPayload {
    public static final Type<ChangeRestorerTargetGradePayload> ID = new Type<>(
            ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "change_restorer_grade")
    );

    public static final StreamCodec<FriendlyByteBuf, ChangeRestorerTargetGradePayload> CODEC =
            CustomPacketPayload.codec(ChangeRestorerTargetGradePayload::write, ChangeRestorerTargetGradePayload::new);

    public ChangeRestorerTargetGradePayload(FriendlyByteBuf buf) {
        this(buf.readInt());
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeInt(targetGrade);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
