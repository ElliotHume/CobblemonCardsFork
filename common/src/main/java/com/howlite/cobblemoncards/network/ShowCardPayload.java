package com.howlite.cobblemoncards.network;

import com.howlite.cobblemoncards.CobblemonCards;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * Payload S2C envoyé aux joueurs PROCHES quand un joueur fait click-droit sur une carte.
 * Ils verront la carte rendue en grand dans le monde à côté du joueur qui la montre.
 */
public record ShowCardPayload(UUID holderUuid, ItemStack card) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ShowCardPayload> ID =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, "show_card"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShowCardPayload> CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeUUID(p.holderUuid());
                        ItemStack.STREAM_CODEC.encode(buf, p.card());
                    },
                    buf -> new ShowCardPayload(buf.readUUID(), ItemStack.STREAM_CODEC.decode(buf))
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
