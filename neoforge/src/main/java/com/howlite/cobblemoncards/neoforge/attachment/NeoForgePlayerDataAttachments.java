package com.howlite.cobblemoncards.neoforge.attachment;

import com.howlite.cobblemoncards.CobblemonCards;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("null")
public class NeoForgePlayerDataAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CobblemonCards.MOD_ID);

    public static final Supplier<AttachmentType<Boolean>> HAS_GUARANTEED_GOD_PACK = ATTACHMENT_TYPES.register(
            "has_guaranteed_god_pack",
            () -> AttachmentType.builder(() -> false).serialize(com.mojang.serialization.Codec.BOOL).copyOnDeath().build()
    );

    public static final Supplier<AttachmentType<Integer>> OPENED_BOOSTERS = ATTACHMENT_TYPES.register(
            "opened_boosters",
            () -> AttachmentType.builder(() -> 0).serialize(com.mojang.serialization.Codec.INT).copyOnDeath().build()
    );

    public static final Supplier<AttachmentType<List<String>>> DISCOVERED_CARDS = ATTACHMENT_TYPES.register(
            "discovered_cards",
            () -> AttachmentType.builder(() -> (List<String>) new java.util.ArrayList<String>())
                    .serialize(com.mojang.serialization.Codec.STRING.listOf())
                    .copyOnDeath()
                    .build()
    );
}
