package com.howlite.cobblemoncards.fabric;

import com.howlite.cobblemoncards.CobblemonCards;
import com.howlite.cobblemoncards.CobblemonCardsConfig;
import com.howlite.cobblemoncards.attachment.PlayerDataAttachments;
import com.howlite.cobblemoncards.block.ModBlocks;
import com.howlite.cobblemoncards.block.entity.AdvancedHoloProjectorBlockEntity;
import com.howlite.cobblemoncards.command.CustomBoosterCommand;
import com.howlite.cobblemoncards.command.GiveCardCommand;
import com.howlite.cobblemoncards.component.CardData;
import com.howlite.cobblemoncards.component.CardStat;
import com.howlite.cobblemoncards.component.ModDataComponents;
import com.howlite.cobblemoncards.item.ModItems;
import com.howlite.cobblemoncards.item.custom.loot.BoosterLootTable;
import com.howlite.cobblemoncards.manager.BoosterPackManager;
import com.howlite.cobblemoncards.menu.AdvancedHoloProjectorMenu;
import com.howlite.cobblemoncards.menu.BinderMenu;
import com.howlite.cobblemoncards.menu.CardCabinetMenu;
import com.howlite.cobblemoncards.network.*;
import com.howlite.cobblemoncards.util.FakemonWhitelistReloader;
import com.howlite.cobblemoncards.util.PlatformHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.howlite.cobblemoncards.event.BinderSpawnModifier;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;

public class FabricCobblemonCards implements ModInitializer {

    @Override
    public void onInitialize() {
        // 1. Initialiser le PlatformHelper d'abord
        PlatformHelper.PlatformHelperHolder.set(new FabricPlatformHelper());

        // 2. Initialiser le mod commun
        CobblemonCards.init();

        // Enregistrer les Accessories pour les Binders et Albums
        if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("accessories")) {
            CobblemonCards.LOGGER.info("CobblemonCards: Registering binders and master album as Accessories!");
            io.wispforest.accessories.api.AccessoriesAPI.registerAccessory(ModItems.LEATHER_BINDER, (io.wispforest.accessories.api.Accessory) ModItems.LEATHER_BINDER);
            io.wispforest.accessories.api.AccessoriesAPI.registerAccessory(ModItems.IRON_BINDER, (io.wispforest.accessories.api.Accessory) ModItems.IRON_BINDER);
            io.wispforest.accessories.api.AccessoriesAPI.registerAccessory(ModItems.GOLD_BINDER, (io.wispforest.accessories.api.Accessory) ModItems.GOLD_BINDER);
            io.wispforest.accessories.api.AccessoriesAPI.registerAccessory(ModItems.DIAMOND_BINDER, (io.wispforest.accessories.api.Accessory) ModItems.DIAMOND_BINDER);
            io.wispforest.accessories.api.AccessoriesAPI.registerAccessory(ModItems.NETHERITE_BINDER, (io.wispforest.accessories.api.Accessory) ModItems.NETHERITE_BINDER);
            io.wispforest.accessories.api.AccessoriesAPI.registerAccessory(ModItems.MASTER_ALBUM, (io.wispforest.accessories.api.Accessory) ModItems.MASTER_ALBUM);
        }

        // 3. Enregistrer les attachements Fabric
        PlayerDataAttachments.register();

        // 4. Enregistrer les offres du marchand ambulant
        registerWanderingTraderOffers();

        // 5. Enregistrer les paquets réseau
        registerPackets();

        // 6. Enregistrer les récepteurs réseau
        registerServerPacketReceivers();

        // 7. Enregistrer les commandes
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            GiveCardCommand.register(dispatcher);
            CustomBoosterCommand.register(dispatcher);
        });

        // 8. Modifier les tables de butin
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (source.isBuiltin() && CobblemonCardsConfig.enableBoosterChestSpawn) {
                String path = key.location().getPath();
                if (key.location().getNamespace().equals("minecraft") && path.startsWith("chests/") && !path.startsWith("chests/village/")) {
                    float chance = CobblemonCardsConfig.boosterChestSpawnChance / 100.0f;
                    if (chance > 0.0f) {
                        LootPool.Builder poolBuilder = LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(ModItems.BOOSTER_PACK))
                                .conditionally(LootItemRandomChanceCondition.randomChance(chance).build());
                        tableBuilder.pool(poolBuilder.build());
                    }
                }
            }
        });

        // 9. Enregistrer le modificateur de spawn
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof PokemonEntity pokemonEntity) {
                BinderSpawnModifier.onEntityLoad(pokemonEntity, world);
            }
        });

        // 10. Enregistrer le reload listener datapack pour la whitelist Fakemon
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(
            new SimpleSynchronousResourceReloadListener() {
                @Override
                public ResourceLocation getFabricId() {
                    return ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, "fakemon_whitelist");
                }

                @Override
                public void onResourceManagerReload(ResourceManager manager) {
                    FakemonWhitelistReloader.loadFrom(manager);
                }
            }
        );
    }

    private void registerPackets() {
        PayloadTypeRegistry.playC2S().register(OpenBinderPayload.ID, OpenBinderPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenBoosterPayload.ID, OpenBoosterPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GiveRewardPayload.ID, GiveRewardPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ToggleProjectorNamePayload.ID, ToggleProjectorNamePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CloseBoosterPayload.ID, CloseBoosterPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(BinderPagePayload.ID, BinderPagePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SortBinderPayload.ID, SortBinderPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(GenerateCardPayload.ID, GenerateCardPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenWorkshopPayload.ID, OpenWorkshopPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncDiscoveredCardsPayload.ID, SyncDiscoveredCardsPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RenderCardPayload.ID, RenderCardPayload.CODEC);
    }

    private void registerServerPacketReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(OpenBinderPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                net.minecraft.server.level.ServerPlayer player = context.player();
                ItemStack binderStack = BinderMenu.findActiveBinder(player);
                if (!binderStack.isEmpty()) {
                    ItemStack finalStack = binderStack;
                    player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                            (containerId, playerInventory, p) -> new BinderMenu(containerId, playerInventory, finalStack),
                            binderStack.getHoverName()
                    ));
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(BinderPagePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                if (context.player().containerMenu instanceof BinderMenu menu) {
                    menu.setPage(payload.page());
                } else if (context.player().containerMenu instanceof CardCabinetMenu menu) {
                    menu.setPage(payload.page());
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(SortBinderPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                if (context.player().containerMenu instanceof BinderMenu menu) {
                    menu.sort(payload.sortMode());
                } else if (context.player().containerMenu instanceof CardCabinetMenu menu) {
                    menu.sort(payload.sortMode());
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(ToggleProjectorNamePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                if (context.player().containerMenu instanceof AdvancedHoloProjectorMenu menu) {
                    if (menu.getContainer() instanceof AdvancedHoloProjectorBlockEntity advancedBE) {
                        advancedBE.toggleShowName();
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(CloseBoosterPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                List<ItemStack> rewards = BoosterPackManager.getAndClearRewards(context.player().getUUID());
                if (rewards != null) {
                    for (ItemStack reward : rewards) {
                        if (!context.player().getInventory().add(reward)) {
                            context.player().drop(reward, false);
                        }
                    }
                    com.howlite.cobblemoncards.util.CardAdvancementManager.checkAdvancements(context.player());
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(GenerateCardPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                net.minecraft.server.level.ServerPlayer player = context.player();
                if (!player.hasPermissions(2)) {
                    player.sendSystemMessage(Component.translatable("message.cobblemon-cards.workshop.no_permission"));
                    return;
                }

                ItemStack cardStack = new ItemStack(ModItems.CARD);

                CardStat stat = CardStat.MOVEMENT_SPEED;
                for (CardStat s : CardStat.values()) {
                    if (s.getSerializedName().equalsIgnoreCase(payload.statName())) {
                        stat = s;
                        break;
                    }
                }

                java.util.Optional<String> background = payload.background().equals("none") ? java.util.Optional.empty() : java.util.Optional.of(payload.background());
                java.util.Optional<String> effect = payload.effect().equals("none") ? java.util.Optional.empty() : java.util.Optional.of(payload.effect());

                if (effect.isPresent() && background.isEmpty()) {
                    background = java.util.Optional.of(com.howlite.cobblemoncards.util.CardUtil.getDefaultBackground(payload.pokemonId()));
                }

                CardData data = new CardData(
                        payload.pokemonId(),
                        payload.isShiny(),
                        payload.rarity(),
                        stat,
                        payload.statValue(),
                        0,
                        background,
                        effect
                );
                cardStack.set(ModDataComponents.CARD_DATA, data);

                if (!player.getInventory().add(cardStack)) {
                    player.drop(cardStack, false);
                }
                com.howlite.cobblemoncards.util.CardAdvancementManager.checkAdvancements(player);
                player.sendSystemMessage(Component.translatable("message.cobblemon-cards.workshop.success"));
            });
        });
    }

    private void registerWanderingTraderOffers() {
        VillagerTrades.ItemListing cardTrade = new VillagerTrades.ItemListing() {
            @Override
            public MerchantOffer getOffer(Entity entity, RandomSource random) {
                int roll = random.nextInt(100);
                String rarity;
                if (roll < 1) rarity = "legendary";
                else if (roll < 5) rarity = "epic";
                else if (roll < 20) rarity = "rare";
                else if (roll < 50) rarity = "uncommon";
                else rarity = "common";

                ItemStack cardStack = BoosterLootTable.getRandomReward(rarity);

                ItemCost cost;
                int quantity;
                if (rarity.equals("common")) {
                    quantity = 10 + random.nextInt(6);
                    cost = new ItemCost(ModItems.CARD_DUST, quantity);
                } else if (rarity.equals("uncommon")) {
                    quantity = 30 + random.nextInt(11);
                    cost = new ItemCost(ModItems.CARD_DUST, quantity);
                } else if (rarity.equals("rare")) {
                    quantity = 1 + random.nextInt(2);
                    cost = new ItemCost(ModItems.CARD_DUST_POUCH, quantity);
                } else if (rarity.equals("epic")) {
                    quantity = 4 + random.nextInt(2);
                    cost = new ItemCost(ModItems.CARD_DUST_POUCH, quantity);
                } else {
                    cost = new ItemCost(ModBlocks.CARD_DUST_SACK.asItem(), 1);
                }

                return new MerchantOffer(cost, cardStack, 1, 5, 0.05F);
            }
        };

        TradeOfferHelper.registerWanderingTraderOffers(1, factories -> {
            for (int i = 0; i < 5; i++) {
                factories.add(cardTrade);
            }
        });

        TradeOfferHelper.registerWanderingTraderOffers(2, factories -> {
            for (int i = 0; i < 2; i++) {
                factories.add(cardTrade);
            }
        });
    }
}
