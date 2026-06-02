package com.howlite.cobblemoncards;

import com.howlite.cobblemoncards.block.ModBlocks;
import com.howlite.cobblemoncards.block.entity.AdvancedHoloProjectorBlockEntity;
import com.howlite.cobblemoncards.block.entity.ModBlockEntities;
import com.howlite.cobblemoncards.component.ModDataComponents;
import com.howlite.cobblemoncards.event.ModEvents;
import com.howlite.cobblemoncards.item.ModCreativeTabs;
import com.howlite.cobblemoncards.item.ModItems;
import com.howlite.cobblemoncards.item.custom.loot.BoosterLootTable;
import com.howlite.cobblemoncards.manager.BoosterPackManager;
import com.howlite.cobblemoncards.menu.AdvancedHoloProjectorMenu;
import com.howlite.cobblemoncards.menu.BinderMenu;
import com.howlite.cobblemoncards.menu.CardCabinetMenu;
import com.howlite.cobblemoncards.menu.ModMenuTypes;
import com.howlite.cobblemoncards.network.BinderPagePayload;
import com.howlite.cobblemoncards.network.CloseBoosterPayload;
import com.howlite.cobblemoncards.network.GiveRewardPayload;
import com.howlite.cobblemoncards.network.OpenBinderPayload;
import com.howlite.cobblemoncards.network.OpenBoosterPayload;
import com.howlite.cobblemoncards.network.SortBinderPayload;
import com.howlite.cobblemoncards.network.ToggleProjectorNamePayload;
import com.howlite.cobblemoncards.network.GenerateCardPayload;
import com.howlite.cobblemoncards.network.OpenWorkshopPayload;
import com.howlite.cobblemoncards.network.SyncDiscoveredCardsPayload;
import com.howlite.cobblemoncards.component.CardData;
import com.howlite.cobblemoncards.component.CardStat;
import com.howlite.cobblemoncards.command.GiveCardCommand;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.slf4j.Logger;
import com.howlite.cobblemoncards.sound.ModSounds;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CobblemonCards implements ModInitializer {
    public static final String MOD_ID = "cobblemon-cards";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Initialisation de la configuration MidnightLib - ON L'INITIALISE ICI POUR LE SERVEUR AUSSI !
        MidnightConfig.init(MOD_ID, CobblemonCardsConfig.class);

        ModDataComponents.register();
        ModItems.register();
        ModBlocks.register();
        ModBlockEntities.register();
        ModMenuTypes.register();
        ModCreativeTabs.register();
        ModEvents.registerEvents();
        ModSounds.registerSounds();

        registerWanderingTraderOffers();

        // Enregistrement de TOUS les paquets
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

        // Réception du paquet pour changer de page dans le classeur ou le meuble
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
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cVous n'avez pas la permission d'utiliser le Card Workshop."));
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
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a[Card Workshop] Carte générée avec succès !"));
            });
        });
        
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            GiveCardCommand.register(dispatcher);
        });
    }

    private void registerWanderingTraderOffers() {
        VillagerTrades.ItemListing cardTrade = new VillagerTrades.ItemListing() {
            @Override
            public MerchantOffer getOffer(Entity entity, RandomSource random) {
                // Random rarity selection (weighted)
                int roll = random.nextInt(100);
                String rarity;
                if (roll < 1) rarity = "legendary"; // 1%
                else if (roll < 5) rarity = "epic"; // 4%
                else if (roll < 20) rarity = "rare"; // 15%
                else if (roll < 50) rarity = "uncommon"; // 30%
                else rarity = "common"; // 50%

                // Generate the card using BoosterLootTable
                ItemStack cardStack = BoosterLootTable.getRandomReward(rarity);

                // Determine the cost based on rarity
                ItemCost cost;
                int quantity;
                if (rarity.equals("common")) {
                    quantity = 10 + random.nextInt(6); // 10 to 15
                    cost = new ItemCost(ModItems.CARD_DUST, quantity);
                } else if (rarity.equals("uncommon")) {
                    quantity = 30 + random.nextInt(11); // 30 to 40
                    cost = new ItemCost(ModItems.CARD_DUST, quantity);
                } else if (rarity.equals("rare")) {
                    quantity = 1 + random.nextInt(2); // 1 to 2
                    cost = new ItemCost(ModItems.CARD_DUST_POUCH, quantity);
                } else if (rarity.equals("epic")) {
                    quantity = 4 + random.nextInt(2); // 4 to 5
                    cost = new ItemCost(ModItems.CARD_DUST_POUCH, quantity);
                } else { // legendary / mythic
                    cost = new ItemCost(ModBlocks.CARD_DUST_SACK.asItem(), 1);
                }

                // Return new MerchantOffer
                return new MerchantOffer(cost, cardStack, 1, 5, 0.05F);
            }
        };

        // Le marchand ambulant choisit 5 échanges aléatoires dans le niveau 1 et 1 dans le niveau 2.
        // Pour être sûr qu'il propose des cartes, on ajoute l'échange plusieurs fois dans la liste ("pondération" forte)
        // et on l'ajoute également au niveau 2 (échanges rares).
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
