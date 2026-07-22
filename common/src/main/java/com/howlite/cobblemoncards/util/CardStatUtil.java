package com.howlite.cobblemoncards.util;

import com.howlite.cobblemoncards.CobblemonCardsConfig;
import com.howlite.cobblemoncards.component.CardData;
import com.howlite.cobblemoncards.component.CardStat;
import com.howlite.cobblemoncards.component.ModDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.List;
public class CardStatUtil {
    
    public static float getPlayerDropBonus(ServerPlayer player) {
        float totalBonus = 0f;
        
        List<EquippedAccessory> accessories = PlatformHelper.INSTANCE.getEquippedAccessories(player);
        com.howlite.cobblemoncards.CobblemonCards.LOGGER.info("[CobblemonCards] Checking player drop bonus for " + player.getName().getString() + ". Accessories found: " + accessories.size());
        
        // Check Platform Accessories (Binders equipped in accessory slots)
        for (EquippedAccessory equipped : accessories) {
            float bonus = getBonusFromStack(equipped.stack());
            com.howlite.cobblemoncards.CobblemonCards.LOGGER.info("[CobblemonCards] Equipped accessory in slot '" + equipped.slotName() + "' stack: " + equipped.stack() + ", bonus: " + bonus);
            totalBonus += bonus;
        }
        
        // Also check main hand and offhand just in case
        float mainHandBonus = getBonusFromStack(player.getItemInHand(InteractionHand.MAIN_HAND));
        if (mainHandBonus > 0f) {
            com.howlite.cobblemoncards.CobblemonCards.LOGGER.info("[CobblemonCards] Main hand has binder drop bonus: " + mainHandBonus);
            totalBonus += mainHandBonus;
        }
        
        float offHandBonus = getBonusFromStack(player.getItemInHand(InteractionHand.OFF_HAND));
        if (offHandBonus > 0f) {
            com.howlite.cobblemoncards.CobblemonCards.LOGGER.info("[CobblemonCards] Off hand has binder drop bonus: " + offHandBonus);
            totalBonus += offHandBonus;
        }
        
        // Apply the stat multiplier from config
        float finalBonus = totalBonus * CobblemonCardsConfig.getStatMultiplier(CardStat.CARD_DROP_CHANCE);
        com.howlite.cobblemoncards.CobblemonCards.LOGGER.info("[CobblemonCards] Final calculated drop bonus: " + finalBonus);
        return finalBonus;
    }
    
    private static float getBonusFromStack(ItemStack stack) {
        float bonus = 0f;
        if (stack == null || stack.isEmpty()) return bonus;

        // Lire depuis BINDER_CONTENTS (nouveau stockage custom)
        List<ItemStack> binderItems = stack.get(ModDataComponents.BINDER_CONTENTS);
        Iterable<ItemStack> contentItems;
        if (binderItems != null) {
            contentItems = binderItems.stream().filter(s -> !s.isEmpty()).toList();
        } else if (stack.has(DataComponents.CONTAINER)) {
            // Fallback sur CONTAINER pour la rétrocompatibilité
            contentItems = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).nonEmptyItems();
        } else {
            return bonus;
        }

        for (ItemStack contentStack : contentItems) {
            CardData cardData = contentStack.get(ModDataComponents.CARD_DATA);
            if (cardData != null && !CardUtil.isCosmeticCard(cardData.pokemonId()) && cardData.stat() == CardStat.CARD_DROP_CHANCE) {
                bonus += cardData.statValue();
            }
        }
        return bonus;
    }
}