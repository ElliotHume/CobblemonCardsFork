package com.howlite.cobblemoncards.event;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.howlite.cobblemoncards.CobblemonCards;
import com.howlite.cobblemoncards.CobblemonCardsConfig;
import com.howlite.cobblemoncards.component.CardData;
import com.howlite.cobblemoncards.component.CardStat;
import com.howlite.cobblemoncards.component.ModDataComponents;
import com.howlite.cobblemoncards.item.ModItems;
import com.howlite.cobblemoncards.item.custom.loot.BoosterLootTable;
import com.howlite.cobblemoncards.util.CardStatUtil;
import kotlin.Unit;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Random;

public class ModEvents {
    private static final Random RANDOM = new Random();

    public static void registerEvents() {
        CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.NORMAL, event -> {
            handlePokemonDrop(event.getPlayer(), event.getPokemon());
            return Unit.INSTANCE;
        });

        CobblemonEvents.BATTLE_FAINTED.subscribe(Priority.NORMAL, event -> {
            if (event.getKilled().getEntity() != null) {
                Pokemon pokemon = event.getKilled().getEntity().getPokemon();
                // Check if it doesn't belong to a player
                if (!pokemon.isPlayerOwned()) {
                    event.getBattle().getPlayers().forEach(player -> {
                        if (player instanceof ServerPlayer serverPlayer) {
                            handlePokemonDrop(serverPlayer, pokemon);
                        }
                    });
                }
            }
            return Unit.INSTANCE;
        });

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
    }

    private static void handlePokemonDrop(ServerPlayer player, Pokemon pokemon) {
        float dropBonus = CardStatUtil.getPlayerDropBonus(player);
        // Base chance is defined in the config (default 1.0f)
        float totalChance = CobblemonCardsConfig.cardDropChance + dropBonus;
        
        CobblemonCards.LOGGER.info("Calculated drop chance for player {}: {} (Base: {}, Bonus: {})", 
                player.getName().getString(), totalChance, CobblemonCardsConfig.cardDropChance, dropBonus);
        
        if (RANDOM.nextFloat() * 100f <= totalChance) {
            CobblemonCards.LOGGER.info("Drop successful! Generating card for {}", pokemon.getSpecies().getName());
            
            String pokemonId = pokemon.getSpecies().getName().toLowerCase();
            boolean isShiny = pokemon.getShiny();
            String rarity = "common";
            
            try {
                Method method = BoosterLootTable.class.getDeclaredMethod("getRandomRarity");
                method.setAccessible(true);
                rarity = (String) method.invoke(null);
            } catch (Exception e) {
                // Ignore and use common
            }
            
            CardStat randomStat = CardStat.values()[RANDOM.nextInt(CardStat.values().length)];
            
            // Stat value cohérente avec la rareté (même barème que les Boosters)
            float statValue = switch (rarity) {
                case "mythic"    -> 0.20f + RANDOM.nextFloat() * 0.05f;
                case "legendary" -> 0.12f + RANDOM.nextFloat() * 0.06f;
                case "epic"      -> 0.08f + RANDOM.nextFloat() * 0.04f;
                case "rare"      -> 0.04f + RANDOM.nextFloat() * 0.03f;
                case "uncommon"  -> 0.015f + RANDOM.nextFloat() * 0.015f;
                default          -> 0.005f + RANDOM.nextFloat() * 0.005f; // common
            };
            if (isShiny) statValue += 0.03f;
            
            ItemStack cardStack = new ItemStack(ModItems.CARD);
            CardData cardData = new CardData(
                    pokemonId,
                    isShiny,
                    rarity,
                    randomStat,
                    statValue,
                    0,
                    Optional.empty(),
                    Optional.empty()
            );
            
            cardStack.set(ModDataComponents.CARD_DATA, cardData);
            
            if (!player.getInventory().add(cardStack)) {
                ItemEntity itemEntity = new ItemEntity(player.serverLevel(), player.getX(), player.getY(), player.getZ(), cardStack);
                player.serverLevel().addFreshEntity(itemEntity);
            }
            
            com.howlite.cobblemoncards.util.CardAdvancementManager.checkAdvancements(player);
            
            player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), 
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 2.0f);
        } else {
            CobblemonCards.LOGGER.info("Drop failed.");
        }
    }
}