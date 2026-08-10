package com.howlite.cobblemoncards.block.entity;

import com.howlite.cobblemoncards.CobblemonCards;
import com.howlite.cobblemoncards.block.ModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    public static final BlockEntityType<GradingStationBlockEntity> GRADING_STATION_BE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, "grading_station_be"),
            BlockEntityType.Builder.of(GradingStationBlockEntity::new, ModBlocks.GRADING_STATION).build(null)
    );

    public static final BlockEntityType<CardRecyclerBlockEntity> CARD_RECYCLER_BE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, "card_recycler_be"),
            BlockEntityType.Builder.of(CardRecyclerBlockEntity::new, ModBlocks.CARD_RECYCLER).build(null)
    );

    public static final BlockEntityType<HoloProjectorBlockEntity> HOLO_PROJECTOR_BE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, "holo_projector_be"),
            BlockEntityType.Builder.of(HoloProjectorBlockEntity::new, ModBlocks.HOLO_PROJECTOR).build(null)
    );

    public static final BlockEntityType<AdvancedHoloProjectorBlockEntity> ADVANCED_HOLO_PROJECTOR_BE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, "advanced_holo_projector_be"),
            BlockEntityType.Builder.of(AdvancedHoloProjectorBlockEntity::new, ModBlocks.ADVANCED_HOLO_PROJECTOR).build(null)
    );

    public static final BlockEntityType<MiniHoloProjectorBlockEntity> MINI_HOLO_PROJECTOR_BE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, "mini_holo_projector_be"),
            BlockEntityType.Builder.of(MiniHoloProjectorBlockEntity::new, ModBlocks.MINI_HOLO_PROJECTOR).build(null)
    );

    public static final BlockEntityType<CardCabinetBlockEntity> CARD_CABINET_BE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, "card_cabinet_be"),
            BlockEntityType.Builder.of(CardCabinetBlockEntity::new, ModBlocks.CARD_CABINET).build(null)
    );

    public static void register() {
        CobblemonCards.LOGGER.info("Registering Block Entities for " + CobblemonCards.MOD_ID);
    }
}
