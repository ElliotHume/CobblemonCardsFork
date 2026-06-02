package com.howlite.cobblemoncards.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.List; // N'oublie pas cet import !

// On demande maintenant une List<ItemStack>
public record GiveRewardPayload(List<ItemStack> rewards) implements CustomPacketPayload {

    public static final Type<GiveRewardPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "give_reward"));

    // On utilise ItemStack.LIST_STREAM_CODEC qui est magique !
    public static final StreamCodec<RegistryFriendlyByteBuf, GiveRewardPayload> CODEC = StreamCodec.composite(
            ItemStack.LIST_STREAM_CODEC,
            GiveRewardPayload::rewards,
            GiveRewardPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}