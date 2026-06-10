package com.howlite.cobblemoncards.item;

import com.howlite.cobblemoncards.item.custom.BinderTier;
import com.howlite.cobblemoncards.item.custom.BoosterPackItem;
import com.howlite.cobblemoncards.item.custom.GradingStationBypassItem;
import com.howlite.cobblemoncards.item.custom.GodPackTicketItem;
import com.howlite.cobblemoncards.item.custom.InstantDexItem;
import com.howlite.cobblemoncards.item.custom.CardStructureDiskItem;
import com.howlite.cobblemoncards.item.custom.CardDexItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import com.howlite.cobblemoncards.util.PlatformHelper;

public class ModItems {

    public static final Item BOOSTER_PACK = new BoosterPackItem(new Item.Properties());
    public static final Item BOOSTER_PACK_GEN1 = new BoosterPackItem("gen1", new Item.Properties());
    public static final Item BOOSTER_PACK_GEN2 = new BoosterPackItem("gen2", new Item.Properties());
    public static final Item BOOSTER_PACK_GEN3 = new BoosterPackItem("gen3", new Item.Properties());
    public static final Item BOOSTER_PACK_GEN4 = new BoosterPackItem("gen4", new Item.Properties());
    public static final Item BOOSTER_PACK_GEN5 = new BoosterPackItem("gen5", new Item.Properties());
    public static final Item BOOSTER_PACK_GEN6 = new BoosterPackItem("gen6", new Item.Properties());
    public static final Item BOOSTER_PACK_GEN7 = new BoosterPackItem("gen7", new Item.Properties());
    public static final Item BOOSTER_PACK_GEN8 = new BoosterPackItem("gen8", new Item.Properties());
    public static final Item BOOSTER_PACK_GEN9 = new BoosterPackItem("gen9", new Item.Properties());
    public static final Item BOOSTER_PACK_FIRE = new BoosterPackItem("fire", new Item.Properties());
    public static final Item BOOSTER_PACK_WATER = new BoosterPackItem("water", new Item.Properties());
    public static final Item BOOSTER_PACK_GRASS = new BoosterPackItem("grass", new Item.Properties());
    public static final Item BOOSTER_PACK_ELECTRIC = new BoosterPackItem("electric", new Item.Properties());
    public static final Item BOOSTER_PACK_ICE = new BoosterPackItem("ice", new Item.Properties());
    public static final Item BOOSTER_PACK_FIGHTING = new BoosterPackItem("fighting", new Item.Properties());
    public static final Item BOOSTER_PACK_POISON = new BoosterPackItem("poison", new Item.Properties());
    public static final Item BOOSTER_PACK_GROUND = new BoosterPackItem("ground", new Item.Properties());
    public static final Item BOOSTER_PACK_FLYING = new BoosterPackItem("flying", new Item.Properties());
    public static final Item BOOSTER_PACK_PSYCHIC = new BoosterPackItem("psychic", new Item.Properties());
    public static final Item BOOSTER_PACK_BUG = new BoosterPackItem("bug", new Item.Properties());
    public static final Item BOOSTER_PACK_ROCK = new BoosterPackItem("rock", new Item.Properties());
    public static final Item BOOSTER_PACK_GHOST = new BoosterPackItem("ghost", new Item.Properties());
    public static final Item BOOSTER_PACK_DRAGON = new BoosterPackItem("dragon", new Item.Properties());
    public static final Item BOOSTER_PACK_STEEL = new BoosterPackItem("steel", new Item.Properties());
    public static final Item BOOSTER_PACK_FAIRY = new BoosterPackItem("fairy", new Item.Properties());
    public static final Item BOOSTER_PACK_DARK = new BoosterPackItem("dark", new Item.Properties());
    public static final Item BOOSTER_PACK_NORMAL = new BoosterPackItem("normal", new Item.Properties());
    public static final Item CARD = PlatformHelper.INSTANCE.createCardItem(new Item.Properties().stacksTo(1));

    // Nouveaux Binders
    public static final Item LEATHER_BINDER = PlatformHelper.INSTANCE.createBinderItem(BinderTier.LEATHER, new Item.Properties().stacksTo(1));
    public static final Item IRON_BINDER = PlatformHelper.INSTANCE.createBinderItem(BinderTier.IRON, new Item.Properties().stacksTo(1));
    public static final Item GOLD_BINDER = PlatformHelper.INSTANCE.createBinderItem(BinderTier.GOLD, new Item.Properties().stacksTo(1));
    public static final Item DIAMOND_BINDER = PlatformHelper.INSTANCE.createBinderItem(BinderTier.DIAMOND, new Item.Properties().stacksTo(1));
    public static final Item NETHERITE_BINDER = PlatformHelper.INSTANCE.createBinderItem(BinderTier.NETHERITE, new Item.Properties().stacksTo(1).fireResistant());
    public static final Item MASTER_ALBUM = PlatformHelper.INSTANCE.createMasterAlbumItem(new Item.Properties().stacksTo(1));

    public static final Item GRADING_STATION_BYPASS = new GradingStationBypassItem(new Item.Properties().stacksTo(64));
    public static final Item GOD_PACK_TICKET = new GodPackTicketItem(new Item.Properties().stacksTo(16));

    public static final Item CARD_DUST = new Item(new Item.Properties());
    public static final Item CARD_DUST_POUCH = new Item(new Item.Properties());
    public static final Item CHAMPION_RIBBON = new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC));

    public static final Item FRAME_COMMON = new Item(new Item.Properties());
    public static final Item FRAME_UNCOMMON = new Item(new Item.Properties());
    public static final Item FRAME_RARE = new Item(new Item.Properties());
    public static final Item FRAME_EPIC = new Item(new Item.Properties());
    public static final Item FRAME_LEGENDARY = new Item(new Item.Properties());
    public static final Item FRAME_SHINY = new Item(new Item.Properties());

    public static final Item CARD_STRUCTURE_DISK = new CardStructureDiskItem(new Item.Properties().stacksTo(1));
    public static final Item INSTANT_DEX = new InstantDexItem(new Item.Properties().stacksTo(1));
    public static final Item CARD_DEX = new CardDexItem(new Item.Properties().stacksTo(1));

    public static void register() {
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack"), BOOSTER_PACK);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_gen1"), BOOSTER_PACK_GEN1);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_gen2"), BOOSTER_PACK_GEN2);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_gen3"), BOOSTER_PACK_GEN3);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_gen4"), BOOSTER_PACK_GEN4);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_gen5"), BOOSTER_PACK_GEN5);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_gen6"), BOOSTER_PACK_GEN6);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_gen7"), BOOSTER_PACK_GEN7);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_gen8"), BOOSTER_PACK_GEN8);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_gen9"), BOOSTER_PACK_GEN9);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_fire"), BOOSTER_PACK_FIRE);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_water"), BOOSTER_PACK_WATER);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_grass"), BOOSTER_PACK_GRASS);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_electric"), BOOSTER_PACK_ELECTRIC);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_ice"), BOOSTER_PACK_ICE);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_fighting"), BOOSTER_PACK_FIGHTING);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_poison"), BOOSTER_PACK_POISON);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_ground"), BOOSTER_PACK_GROUND);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_flying"), BOOSTER_PACK_FLYING);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_psychic"), BOOSTER_PACK_PSYCHIC);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_bug"), BOOSTER_PACK_BUG);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_rock"), BOOSTER_PACK_ROCK);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_ghost"), BOOSTER_PACK_GHOST);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_dragon"), BOOSTER_PACK_DRAGON);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_steel"), BOOSTER_PACK_STEEL);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_fairy"), BOOSTER_PACK_FAIRY);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_dark"), BOOSTER_PACK_DARK);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "booster_pack_normal"), BOOSTER_PACK_NORMAL);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "card"), CARD);

        // Enregistrement des nouveaux binders
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "leather_binder"), LEATHER_BINDER);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "iron_binder"), IRON_BINDER);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "gold_binder"), GOLD_BINDER);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "diamond_binder"), DIAMOND_BINDER);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "netherite_binder"), NETHERITE_BINDER);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "master_album"), MASTER_ALBUM);

        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "grading_station_bypass"), GRADING_STATION_BYPASS);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "god_pack_ticket"), GOD_PACK_TICKET);

        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "card_dust"), CARD_DUST);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "card_dust_pouch"), CARD_DUST_POUCH);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "champion_ribbon"), CHAMPION_RIBBON);

        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "frame_common"), FRAME_COMMON);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "frame_uncommon"), FRAME_UNCOMMON);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "frame_rare"), FRAME_RARE);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "frame_epic"), FRAME_EPIC);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "frame_legendary"), FRAME_LEGENDARY);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "frame_shiny"), FRAME_SHINY);

        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "card_structure_disk"), CARD_STRUCTURE_DISK);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "instant_dex"), INSTANT_DEX);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "card_dex"), CARD_DEX);
    }
}