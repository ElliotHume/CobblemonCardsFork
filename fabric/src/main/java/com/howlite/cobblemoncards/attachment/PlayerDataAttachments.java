package com.howlite.cobblemoncards.attachment;

import com.howlite.cobblemoncards.CobblemonCards;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.ResourceLocation;

public class PlayerDataAttachments {
    public static final AttachmentType<Boolean> HAS_GUARANTEED_GOD_PACK = AttachmentRegistry.createPersistent(
            ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, "has_guaranteed_god_pack"),
            com.mojang.serialization.Codec.BOOL
    );

    public static final AttachmentType<Integer> OPENED_BOOSTERS = AttachmentRegistry.createPersistent(
            ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, "opened_boosters"),
            com.mojang.serialization.Codec.INT
    );

    public static final AttachmentType<java.util.List<String>> DISCOVERED_CARDS = AttachmentRegistry.createPersistent(
            ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, "discovered_cards"),
            com.mojang.serialization.Codec.STRING.listOf()
    );

    public static void register() {
        // Just to load the class and register the attachment
    }
}
