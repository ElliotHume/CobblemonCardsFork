package com.howlite.cobblemoncards.event;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.howlite.cobblemoncards.CobblemonCardsConfig;
import com.howlite.cobblemoncards.component.CardData;
import com.howlite.cobblemoncards.component.CardStat;
import com.howlite.cobblemoncards.component.ModDataComponents;
import com.howlite.cobblemoncards.item.custom.BinderItem;
import com.howlite.cobblemoncards.util.PlatformHelper;
import com.howlite.cobblemoncards.util.EquippedAccessory;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class BinderSpawnModifier {
    public static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-cards");
    private static final Random RANDOM = new Random();

    public static void onEntityLoad(PokemonEntity pokemonEntity, net.minecraft.server.level.ServerLevel world) {
        Pokemon pokemon = pokemonEntity.getPokemon();

        // On vérifie si le Pokémon est sauvage (pas de propriétaire)
        if (pokemon.getOwnerUUID() == null) {
            Player nearestPlayer = world.getNearestPlayer(pokemonEntity, 64.0);

            if (nearestPlayer instanceof ServerPlayer serverPlayer) {
                for (EquippedAccessory equipped : PlatformHelper.INSTANCE.getEquippedAccessories(serverPlayer)) {
                    String slotId = equipped.slotName();
                    if ((slotId.equals("belt") || slotId.equals("legs")) && equipped.stack().getItem() instanceof BinderItem) {
                        handleSpawnModification(pokemonEntity, pokemon, equipped.stack());
                    }
                }
            }
        }
    }

    private static void handleSpawnModification(PokemonEntity pokemonEntity, Pokemon pokemon, ItemStack binderStack) {
        // Lire les cartes depuis BINDER_CONTENTS (nouveau stockage custom)
        List<ItemStack> binderItems = binderStack.get(ModDataComponents.BINDER_CONTENTS);
        Iterable<ItemStack> contentItems;
        if (binderItems != null) {
            contentItems = binderItems.stream().filter(s -> !s.isEmpty()).toList();
        } else {
            contentItems = binderStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).nonEmptyItems();
        }
        Map<CardStat, Float> spawnStats = new EnumMap<>(CardStat.class);

        // On calcule les totaux pour les stats de spawn uniquement
        for (ItemStack contentStack : contentItems) {
            CardData cardData = contentStack.get(ModDataComponents.CARD_DATA);
            if (cardData != null && !com.howlite.cobblemoncards.util.CardUtil.isCosmeticCard(cardData.pokemonId()) && cardData.stat().getSerializedName().endsWith("_spawn")) {
                spawnStats.merge(cardData.stat(), cardData.statValue(), Float::sum);
            }
        }

        // On trouve la stat de spawn la plus élevée
        CardStat bestStat = null;
        float totalSpawnChance = 0;

        for (Map.Entry<CardStat, Float> entry : spawnStats.entrySet()) {
            if (entry.getValue() > totalSpawnChance) {
                totalSpawnChance = entry.getValue();
                bestStat = entry.getKey();
            }
        }

        if (bestStat != null && totalSpawnChance > 0) {
            ElementalType targetType = getElementalType(bestStat);
            if (targetType != null) {
                // Si le Pokémon n'est pas déjà du type cible
                if (pokemon.getPrimaryType() != targetType && pokemon.getSecondaryType() != targetType) {
                    // Cap à 30% max : transformer 1 Pokémon sur 3 reste fun sans être trivial
                    float finalChance = Math.min(30.0f, totalSpawnChance * CobblemonCardsConfig.globalStatMultiplier);

                    // Lancer de dé (0-100)
                    if (RANDOM.nextFloat() * 100 < finalChance) {
                        transformPokemon(pokemonEntity, pokemon, targetType, finalChance);
                    }
                }
            }
        }
    }

    private static void transformPokemon(PokemonEntity pokemonEntity, Pokemon oldPokemon, ElementalType targetType, float finalChance) {
        // Filtrer les espèces ayant le type cible
        List<Species> possibleSpecies = PokemonSpecies.getImplemented().stream()
                .filter(species -> species.getPrimaryType() == targetType || species.getSecondaryType() == targetType)
                .collect(Collectors.toList());

        if (!possibleSpecies.isEmpty()) {
            // Calcul du poids total de toutes les espèces possibles
            double totalWeight = 0;
            for (Species species : possibleSpecies) {
                totalWeight += getWeight(species);
            }

            // Sélection pondérée
            double randomValue = RANDOM.nextDouble() * totalWeight;
            double currentWeight = 0;
            Species selectedSpecies = possibleSpecies.get(0);

            for (Species species : possibleSpecies) {
                currentWeight += getWeight(species);
                if (currentWeight >= randomValue) {
                    selectedSpecies = species;
                    break;
                }
            }

            Pokemon newPokemon = selectedSpecies.create(oldPokemon.getLevel());
            pokemonEntity.setPokemon(newPokemon);

            LOGGER.info("[CobblemonCards] BINGO ! Le Binder a transformé un sauvage ({}) en type {} ({}) grâce à {}% de chance !",
                    oldPokemon.getSpecies().getName(), targetType.getName(), selectedSpecies.getName(), finalChance);
        }
    }

    private static double getWeight(Species species) {
        // Si c'est un légendaire ou un fabuleux, on réduit drastiquement sa chance
        if (species.getLabels().contains("legendary") || species.getLabels().contains("mythical")) {
            return 0.1; // Très rare (1000x moins de chance qu'un commun)
        }

        // On utilise le Total des Stats de Base (BST) comme indicateur de rareté
        int bst = species.getBaseStats().values().stream().mapToInt(Integer::intValue).sum();
        
        if (bst >= 600) return 2.0;   // Pseudo-légendaires (Dracolosse, Tyranocif, etc.)
        if (bst >= 500) return 10.0;  // Pokémon puissants / évolutions finales
        if (bst >= 400) return 40.0;  // Pokémon moyens / évolutions intermédiaires
        return 100.0;                 // Pokémon communs / de base (Pidgey, Rattata, etc.)
    }

    private static ElementalType getElementalType(CardStat stat) {
        String typeName = stat.getSerializedName().replace("_spawn", "");
        return ElementalTypes.get(typeName);
    }
}
