package com.howlite.cobblemoncards.item.custom;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.howlite.cobblemoncards.component.CardData;
import com.howlite.cobblemoncards.component.CardStat;
import com.howlite.cobblemoncards.component.DiskData;
import com.howlite.cobblemoncards.component.ModDataComponents;
import com.howlite.cobblemoncards.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InstantDexItem extends Item {

    private static final int MAX_SCANS = 5;

    public InstantDexItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (player.level().isClientSide) {
            return InteractionResult.PASS;
        }

        if (interactionTarget instanceof PokemonEntity pokemonEntity) {
            int diskSlot = findStructureDiskSlot(player);
            if (diskSlot == -1) {
                player.displayClientMessage(Component.translatable("message.cobblemon-cards.instant_dex.disk_required").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }

            ItemStack diskStack = player.getInventory().getItem(diskSlot);
            Pokemon pokemon = pokemonEntity.getPokemon();

            if (pokemon.getOwnerUUID() != null) {
                player.displayClientMessage(Component.translatable("message.cobblemon-cards.instant_dex.wild_only").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }

            DiskData diskData = diskStack.getOrDefault(ModDataComponents.DISK_DATA, DiskData.empty());
            List<UUID> scannedPokemon = new ArrayList<>(diskData.scannedPokemon());
            String speciesName = pokemon.getSpecies().getName().toLowerCase();

            // Vérifier si c'est la même espèce
            if (diskData.targetSpecies().isPresent() && !diskData.targetSpecies().get().equals(speciesName)) {
                player.displayClientMessage(Component.translatable("message.cobblemon-cards.instant_dex.wrong_species", diskData.targetSpecies().get()).withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }

            if (scannedPokemon.contains(pokemonEntity.getUUID())) {
                player.displayClientMessage(Component.translatable("message.cobblemon-cards.instant_dex.already_scanned").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }

            scannedPokemon.add(pokemonEntity.getUUID());
            int newCount = scannedPokemon.size();

            player.level().playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_IN, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.level().addParticle(ParticleTypes.FLASH, interactionTarget.getX(), interactionTarget.getY() + interactionTarget.getBbHeight() / 2.0, interactionTarget.getZ(), 0, 0, 0);

            if (newCount < MAX_SCANS) {
                diskStack.set(ModDataComponents.DISK_DATA, new DiskData(diskData.dust(), newCount, scannedPokemon, Optional.of(speciesName)));
                player.displayClientMessage(Component.translatable("message.cobblemon-cards.instant_dex.scan_success", newCount, MAX_SCANS).withStyle(ChatFormatting.GREEN), true);

                if (pokemon.getShiny()) {
                    player.sendSystemMessage(Component.translatable("message.cobblemon-cards.instant_dex.shiny_detected").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                }
            } else {
                int dustAmount = diskData.dust();
                diskStack.shrink(1); // Consommer le disque entier car on génère la carte

                ItemStack card = generateCard(player, pokemon, dustAmount);
                if (!player.getInventory().add(card)) {
                    player.drop(card, false);
                }

                player.displayClientMessage(Component.translatable("message.cobblemon-cards.instant_dex.capture_complete").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), true);
                player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
            }

            return InteractionResult.SUCCESS;
        } else if (interactionTarget instanceof Player scannedPlayer) {
            int diskSlot = findStructureDiskSlot(player);
            if (diskSlot == -1) {
                player.displayClientMessage(Component.translatable("message.cobblemon-cards.instant_dex.disk_required").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }

            ItemStack diskStack = player.getInventory().getItem(diskSlot);
            diskStack.shrink(1); // Consommer le disque entier car on génère la carte

            ItemStack card = generatePlayerCard(scannedPlayer);
            if (!player.getInventory().add(card)) {
                player.drop(card, false);
            }

            player.displayClientMessage(Component.translatable("message.cobblemon-cards.instant_dex.player_capture_complete", scannedPlayer.getName().getString()).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), true);
            player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.level().addParticle(ParticleTypes.FLASH, interactionTarget.getX(), interactionTarget.getY() + interactionTarget.getBbHeight() / 2.0, interactionTarget.getZ(), 0, 0, 0);

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private int findStructureDiskSlot(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(ModItems.CARD_STRUCTURE_DISK)) {
                return i;
            }
        }
        return -1;
    }

    private ItemStack generateCard(Player player, Pokemon pokemon, int dustAmount) {
        ItemStack card = new ItemStack(ModItems.CARD);
        
        boolean isFullMoon = player.level().getMoonPhase() == 0 && player.level().isNight();
        boolean hasDarkness = player.hasEffect(net.minecraft.world.effect.MobEffects.DARKNESS);
        
        String species;
        boolean isShiny;
        String rarity;
        CardStat stat;
        float statValue;
        int grade;
        
        if (isFullMoon && hasDarkness) {
            species = "missingno";
            isShiny = true;
            rarity = "mythic";
            stat = CardStat.values()[(int) (Math.random() * CardStat.values().length)];
            statValue = 0.666f; // Glitched 66.6% value
            grade = 10; // Perfect Grade 10
            
            // Play a scary glitch sound at a lower pitch to reward the player!
            player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_SCREAM, SoundSource.PLAYERS, 1.0F, 0.5F);
        } else {
            species = pokemon.getSpecies().getName().toLowerCase();
            isShiny = pokemon.getShiny();
            
            rarity = "common";
            if (dustAmount >= 1000) {
                rarity = "mythic";
            } else if (dustAmount >= 500) {
                rarity = "legendary";
            } else if (dustAmount >= 200) {
                rarity = "epic";
            } else if (dustAmount >= 50) {
                rarity = "rare";
            } else if (dustAmount >= 10) {
                rarity = "uncommon";
            }
            
            stat = CardStat.values()[(int) (Math.random() * CardStat.values().length)];
            // Stat value cohérente avec la rareté déterminée — toujours positive
            statValue = switch (rarity) {
                case "mythic"    -> 0.20f + (float) Math.random() * 0.05f; // ×10 → +2.0 à +2.5
                case "legendary" -> 0.12f + (float) Math.random() * 0.06f; // ×10 → +1.2 à +1.8
                case "epic"      -> 0.08f + (float) Math.random() * 0.04f; // ×10 → +0.8 à +1.2
                case "rare"      -> 0.04f + (float) Math.random() * 0.03f; // ×10 → +0.4 à +0.7
                case "uncommon"  -> 0.015f + (float) Math.random() * 0.015f; // ×10 → +0.15 à +0.3
                default          -> 0.005f + (float) Math.random() * 0.005f; // ×10 → +0.05 à +0.10
            };
            // Bonus shiny
            if (isShiny) statValue += 0.03f;
            grade = 0;
        }

        CardData data = new CardData(
                species,
                isShiny,
                rarity,
                stat,
                statValue,
                grade,
                Optional.empty(),
                Optional.empty()
        );

        card.set(ModDataComponents.CARD_DATA, data);
        return card;
    }

    private ItemStack generatePlayerCard(Player scannedPlayer) {
        ItemStack card = new ItemStack(ModItems.CARD);
        String species = "player_" + scannedPlayer.getUUID().toString() + "_" + scannedPlayer.getGameProfile().getName();
        
        boolean isShiny = true;
        String rarity = "mythic";
        CardStat stat = CardStat.values()[0]; // Stat non utilisée (carte cosmétique)
        float statValue = 0.0f; // Carte cosmétique : aucune stat
        int grade = 10;
        
        CardData data = new CardData(
                species,
                isShiny,
                rarity,
                stat,
                statValue,
                grade,
                Optional.empty(),
                Optional.empty()
        );

        card.set(ModDataComponents.CARD_DATA, data);
        return card;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.cobblemon-cards.instant_dex.tool").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.cobblemon-cards.instant_dex.requires_disk").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}