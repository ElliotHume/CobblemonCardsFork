package com.howlite.cobblemoncards.block;

import com.howlite.cobblemoncards.CobblemonCards;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {

    public static final Block GRADING_STATION = registerBlock("grading_station",
            new GradingStationBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .noOcclusion()
            ));

    public static final Block CARD_RECYCLER = registerBlock("card_recycler",
            new CardRecyclerBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
            ));

    public static final Block CARD_DUST_SACK = registerBlock("card_dust_sack",
            new Block(BlockBehaviour.Properties.of()
                    .strength(0.5f)
                    .sound(SoundType.GRASS)
            ));

    public static final Block HOLO_PROJECTOR = registerBlock("holo_projector",
            new HoloProjectorBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .noOcclusion()
            ));

    public static final Block ADVANCED_HOLO_PROJECTOR = registerBlock("advanced_holo_projector",
            new AdvancedHoloProjectorBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .noOcclusion()
            ));

    public static final Block CARD_CABINET = registerBlock("card_cabinet",
            new CardCabinetBlock(BlockBehaviour.Properties.of()
                    .strength(2.5f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            ));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        if (name.equals("holo_projector") || name.equals("advanced_holo_projector") || name.equals("card_cabinet")) {
            Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, name),
                    new com.howlite.cobblemoncards.item.custom.DescribedBlockItem(block, "tooltip.cobblemon-cards." + name + ".description", new Item.Properties()));
        } else {
            Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, name),
                    new BlockItem(block, new Item.Properties()));
        }
    }

    public static void register() {
        CobblemonCards.LOGGER.info("Registering ModBlocks for " + CobblemonCards.MOD_ID);
    }
}
