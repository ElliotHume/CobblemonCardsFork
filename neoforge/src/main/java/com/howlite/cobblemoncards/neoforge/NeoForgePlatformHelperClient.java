package com.howlite.cobblemoncards.neoforge;

import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

@SuppressWarnings("null")
public class NeoForgePlatformHelperClient {
    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }
}
