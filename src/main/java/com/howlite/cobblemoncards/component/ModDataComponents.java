package com.howlite.cobblemoncards.component;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class ModDataComponents {

    public static final DataComponentType<CardData> CARD_DATA = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "card_data"),
            DataComponentType.<CardData>builder()
                    .persistent(CardData.CODEC) // On remplace "codec" par "persistent"
                    .networkSynchronized(CardData.STREAM_CODEC) // Indispensable pour les visuels !
                    .build()
    );

    public static final DataComponentType<DiskData> DISK_DATA = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "disk_data"),
            DataComponentType.<DiskData>builder()
                    .persistent(DiskData.CODEC)
                    .networkSynchronized(DiskData.STREAM_CODEC)
                    .build()
    );

    public static void register() {
        // Méthode vide juste pour charger la classe
    }
}