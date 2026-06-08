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
        
        // Check Platform Accessories (Binders equipped in accessory slots)
        for (EquippedAccessory equipped : PlatformHelper.INSTANCE.getEquippedAccessories(player)) {
            totalBonus += getBonusFromStack(equipped.stack());
        }
        
        // Also check main hand and offhand just in case
        totalBonus += getBonusFromStack(player.getItemInHand(InteractionHand.MAIN_HAND));
        totalBonus += getBonusFromStack(player.getItemInHand(InteractionHand.OFF_HAND));
        
        // Apply the global stat multiplier from config
        return totalBonus * CobblemonCardsConfig.globalStatMultiplier;
    }
    
    private static float getBonusFromStack(ItemStack stack) {
        float bonus = 0f;
        if (stack != null && !stack.isEmpty() && stack.has(DataComponents.CONTAINER)) {
            ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            for (ItemStack contentStack : contents.nonEmptyItems()) {
                CardData cardData = contentStack.get(ModDataComponents.CARD_DATA);
                if (cardData != null && !cardData.pokemonId().startsWith("player_") && cardData.stat() == CardStat.CARD_DROP_CHANCE) {
                    bonus += cardData.statValue();
                }
            }
        }
        return bonus;
    }
}