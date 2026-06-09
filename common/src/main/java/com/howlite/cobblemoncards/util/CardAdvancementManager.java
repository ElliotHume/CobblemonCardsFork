package com.howlite.cobblemoncards.util;

import com.howlite.cobblemoncards.component.CardData;
import com.howlite.cobblemoncards.component.ModDataComponents;
import com.howlite.cobblemoncards.item.ModItems;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import com.cobblemon.mod.common.pokemon.Species;
import com.howlite.cobblemoncards.item.custom.loot.BoosterLootTable;

import java.util.HashSet;
import java.util.Set;

public class CardAdvancementManager {

    /**
     * Programmatically awards an advancement to a player if they don't have it yet.
     */
    public static void grantAdvancement(ServerPlayer player, String advancementId) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("cobblemon-cards", advancementId);
        AdvancementHolder advancement = player.serverLevel().getServer().getAdvancements().get(id);
        if (advancement != null) {
            AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
            if (!progress.isDone()) {
                for (String criterion : progress.getRemainingCriteria()) {
                    player.getAdvancements().award(advancement, criterion);
                }
            }
        }
    }

    /**
     * Checks the player's inventory for card advancements.
     * This includes:
     * - First Shiny card
     * - Grade 10 card
     * - Kanto Starter set (Bulbasaur, Charmander, Squirtle)
     * - Complete Collection (150 unique species cards)
     */
    public static void checkAdvancements(ServerPlayer player) {
        if (player == null || player.level().isClientSide()) return;

        // Ensure root advancement is granted
        grantAdvancement(player, "root");

        boolean hasShiny = false;
        boolean hasGrade10 = false;
        boolean hasBulbasaur = false;
        boolean hasCharmander = false;
        boolean hasSquirtle = false;
        Set<String> uniqueSpecies = new HashSet<>();

        java.util.List<String> discovered = new java.util.ArrayList<>(PlatformHelper.INSTANCE.getDiscoveredCards(player));
        boolean discoveredChanged = false;

        // Scan the player's inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                // If it's a card
                CardData data = stack.get(ModDataComponents.CARD_DATA);
                if (data != null) {
                    processCard(data, uniqueSpecies);
                    if (data.isShiny()) hasShiny = true;
                    if (data.grade() >= 10) hasGrade10 = true;
                    
                    String pokemonId = data.pokemonId().toLowerCase();
                    if (!discovered.contains(pokemonId)) {
                        discovered.add(pokemonId);
                        discoveredChanged = true;
                    }
                    if (pokemonId.equals("bulbasaur")) hasBulbasaur = true;
                    else if (pokemonId.equals("charmander")) hasCharmander = true;
                    else if (pokemonId.equals("squirtle")) hasSquirtle = true;
                }
                
                // If it's a binder/album, scan its items as well!
                if (stack.has(net.minecraft.core.component.DataComponents.CONTAINER)) {
                    net.minecraft.world.item.component.ItemContainerContents contents = 
                        stack.getOrDefault(net.minecraft.core.component.DataComponents.CONTAINER, 
                        net.minecraft.world.item.component.ItemContainerContents.EMPTY);
                    for (ItemStack innerStack : contents.nonEmptyItems()) {
                        CardData innerData = innerStack.get(ModDataComponents.CARD_DATA);
                        if (innerData != null) {
                            processCard(innerData, uniqueSpecies);
                            if (innerData.isShiny()) hasShiny = true;
                            if (innerData.grade() >= 10) hasGrade10 = true;
                            
                            String pokemonId = innerData.pokemonId().toLowerCase();
                            if (!discovered.contains(pokemonId)) {
                                discovered.add(pokemonId);
                                discoveredChanged = true;
                            }
                            if (pokemonId.equals("bulbasaur")) hasBulbasaur = true;
                            else if (pokemonId.equals("charmander")) hasCharmander = true;
                            else if (pokemonId.equals("squirtle")) hasSquirtle = true;
                        }
                    }
                }
            }
        }

        if (discoveredChanged) {
            PlatformHelper.INSTANCE.setDiscoveredCards(player, discovered);
        }

        // Award advancements accordingly
        if (hasShiny) {
            grantAdvancement(player, "first_shiny");
        }
        if (hasGrade10) {
            grantAdvancement(player, "master_evaluator");
        }
        
        if (hasBulbasaur && hasCharmander && hasSquirtle) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "kanto_starter");
            AdvancementHolder advancement = player.serverLevel().getServer().getAdvancements().get(id);
            if (advancement != null) {
                AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
                if (!progress.isDone()) {
                    grantAdvancement(player, "kanto_starter");
                    // Reward a Champion's Ribbon!
                    ItemStack ribbon = new ItemStack(ModItems.CHAMPION_RIBBON);
                    if (!player.getInventory().add(ribbon)) {
                        player.drop(ribbon, false);
                    }
                }
            }
        }

        if (uniqueSpecies.size() >= 150) {
            grantAdvancement(player, "complete_collection");
        }

        // --- New Advancements for Generations, National Dex, Regionals, and Megas ---
        try {
            java.util.List<String> allPokemon = BoosterLootTable.getPokemonIds();
            
            java.util.Set<String> discoveredSet = new java.util.HashSet<>();
            for (String id : discovered) {
                discoveredSet.add(id.toLowerCase());
            }

            // Initialize sets for each category
            java.util.Set<String> gen1Required = new java.util.HashSet<>();
            java.util.Set<String> gen2Required = new java.util.HashSet<>();
            java.util.Set<String> gen3Required = new java.util.HashSet<>();
            java.util.Set<String> gen4Required = new java.util.HashSet<>();
            java.util.Set<String> gen5Required = new java.util.HashSet<>();
            java.util.Set<String> gen6Required = new java.util.HashSet<>();
            java.util.Set<String> gen7Required = new java.util.HashSet<>();
            java.util.Set<String> gen8Required = new java.util.HashSet<>();
            java.util.Set<String> gen9Required = new java.util.HashSet<>();
            java.util.Set<String> nationalRequired = new java.util.HashSet<>();
            java.util.Set<String> regionalsRequired = new java.util.HashSet<>();
            java.util.Set<String> megasRequired = new java.util.HashSet<>();

            // Sets of species for regionals and megas
            java.util.Set<String> alolan = java.util.Set.of("vulpix", "ninetales", "sandshrew", "sandslash", "raichu", "meowth", "persian", "geodude", "graveler", "golem", "grimer", "muk", "exeggutor", "marowak");
            java.util.Set<String> galarian = java.util.Set.of("zigzagoon", "linoone", "ponyta", "rapidash", "farfetchd", "weezing", "mr_mime", "corsola", "darumaka", "darmanitan", "yamask", "stunfisk", "slowpoke", "slowbro", "slowking", "articuno", "zapdos", "moltres");
            java.util.Set<String> hisuian = java.util.Set.of("growlithe", "arcanine", "voltorb", "electrode", "typhlosion", "qwilfish", "sneasel", "zorua", "zoroark", "braviary", "sliggoo", "goodra", "avalugg", "decidueye", "samurott", "lilligant", "basculin");
            java.util.Set<String> mega = java.util.Set.of("venusaur", "charizard", "blastoise", "alakazam", "gengar", "kangaskhan", "pinsir", "gyarados", "aerodactyl", "mewtwo", "ampharos", "scizor", "heracross", "tyranitar", "blaziken", "gardevoir", "mawile", "aggron", "medicham", "manectric", "banette", "absol", "garchomp", "lucario", "abomasnow", "beedrill", "pidgeot", "steelix", "sceptile", "swampert", "sableye", "sharpedo", "camerupt", "altaria", "glalie", "salamence", "metagross", "latias", "latios", "rayquaza", "lopunny", "gallade", "audino", "diancie");

            for (String id : allPokemon) {
                String pIdLower = id.toLowerCase();
                
                // Build Regional requirements
                if (alolan.contains(pIdLower)) regionalsRequired.add(pIdLower + "_alolan");
                if (galarian.contains(pIdLower)) regionalsRequired.add(pIdLower + "_galarian");
                if (hisuian.contains(pIdLower)) regionalsRequired.add(pIdLower + "_hisuian");

                // Build Mega requirements
                if (mega.contains(pIdLower)) {
                    if (pIdLower.equals("charizard") || pIdLower.equals("mewtwo")) {
                        megasRequired.add(pIdLower + "_mega_x");
                        megasRequired.add(pIdLower + "_mega_y");
                    } else {
                        megasRequired.add(pIdLower + "_mega");
                    }
                }

                // Build G1-G9 and National requirements based on Cobblemon registry
                try {
                    String cleanId = id.replace("’", "").replace("'", "");
                    Species species = com.howlite.cobblemoncards.util.CardUtil.getSpecies(cleanId);

                    if (species != null) {
                        int dexNum = species.getNationalPokedexNumber();
                        nationalRequired.add(pIdLower);

                        if (dexNum <= 151) gen1Required.add(pIdLower);
                        else if (dexNum <= 251) gen2Required.add(pIdLower);
                        else if (dexNum <= 386) gen3Required.add(pIdLower);
                        else if (dexNum <= 493) gen4Required.add(pIdLower);
                        else if (dexNum <= 649) gen5Required.add(pIdLower);
                        else if (dexNum <= 721) gen6Required.add(pIdLower);
                        else if (dexNum <= 809) gen7Required.add(pIdLower);
                        else if (dexNum <= 898) gen8Required.add(pIdLower);
                        else if (dexNum <= 1025) gen9Required.add(pIdLower);
                    }
                } catch (Exception ignored) {}
            }

            // Verify and grant G1-G9 advancements
            if (!gen1Required.isEmpty() && discoveredSet.containsAll(gen1Required)) {
                grantAdvancement(player, "complete_gen1");
            }
            if (!gen2Required.isEmpty() && discoveredSet.containsAll(gen2Required)) {
                grantAdvancement(player, "complete_gen2");
            }
            if (!gen3Required.isEmpty() && discoveredSet.containsAll(gen3Required)) {
                grantAdvancement(player, "complete_gen3");
            }
            if (!gen4Required.isEmpty() && discoveredSet.containsAll(gen4Required)) {
                grantAdvancement(player, "complete_gen4");
            }
            if (!gen5Required.isEmpty() && discoveredSet.containsAll(gen5Required)) {
                grantAdvancement(player, "complete_gen5");
            }
            if (!gen6Required.isEmpty() && discoveredSet.containsAll(gen6Required)) {
                grantAdvancement(player, "complete_gen6");
            }
            if (!gen7Required.isEmpty() && discoveredSet.containsAll(gen7Required)) {
                grantAdvancement(player, "complete_gen7");
            }
            if (!gen8Required.isEmpty() && discoveredSet.containsAll(gen8Required)) {
                grantAdvancement(player, "complete_gen8");
            }
            if (!gen9Required.isEmpty() && discoveredSet.containsAll(gen9Required)) {
                grantAdvancement(player, "complete_gen9");
            }

            // Verify and grant National Dex, Regionals, and Megas advancements
            if (!nationalRequired.isEmpty() && discoveredSet.containsAll(nationalRequired)) {
                grantAdvancement(player, "complete_national");
            }
            if (!regionalsRequired.isEmpty() && discoveredSet.containsAll(regionalsRequired)) {
                grantAdvancement(player, "complete_regionals");
            }
            if (!megasRequired.isEmpty() && discoveredSet.containsAll(megasRequired)) {
                grantAdvancement(player, "complete_megas");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processCard(CardData data, Set<String> uniqueSpecies) {
        if (data.pokemonId() != null && !data.pokemonId().isEmpty()) {
            uniqueSpecies.add(data.pokemonId().toLowerCase());
        }
    }

    /**
     * Tracks booster pack openings and awards Booster Addict when they reach 100.
     */
    public static void trackBoosterOpening(ServerPlayer player) {
        if (player == null || player.level().isClientSide()) return;
        
        // Grant root
        grantAdvancement(player, "root");

        int currentCount = PlatformHelper.INSTANCE.getOpenedBoosters(player);
        currentCount++;
        PlatformHelper.INSTANCE.setOpenedBoosters(player, currentCount);

        if (currentCount >= 100) {
            grantAdvancement(player, "booster_addict");
        }
    }
}
