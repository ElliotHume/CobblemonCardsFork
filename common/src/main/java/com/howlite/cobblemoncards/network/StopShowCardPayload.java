package com.howlite.cobblemoncards.network;

import com.howlite.cobblemoncards.CobblemonCards;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/**
 * Payload S2C (Server → Client) envoyé aux joueurs proches quand un joueur ferme son écran d'inspection.
 * Supprime le rendu 3D de la carte pour ce joueur.
 */
public record StopShowCardPayload(UUID holderUuid) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<StopShowCardPayload> ID =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, "stop_show_card"));

    public static final StreamCodec<RegistryFriendlyByteBuf, StopShowCardPayload> CODEC =
            StreamCodec.of(
                    (buf, p) -> buf.writeUUID(p.holderUuid()),
                    buf -> new StopShowCardPayload(buf.readUUID())
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
