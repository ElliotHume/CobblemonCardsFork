package com.howlite.cobblemoncards.fabric.item;

import com.howlite.cobblemoncards.item.custom.BinderItem;
import com.howlite.cobblemoncards.item.custom.BinderTier;
import com.howlite.cobblemoncards.component.CardData;
import com.howlite.cobblemoncards.component.CardStat;
import com.howlite.cobblemoncards.component.ModDataComponents;
import com.howlite.cobblemoncards.CobblemonCardsConfig;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ItemContainerContents;

public class FabricBinderItem extends BinderItem implements Trinket {
    
    public FabricBinderItem(BinderTier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, ResourceLocation id) {
        com.howlite.cobblemoncards.CobblemonCards.LOGGER.info("CobblemonCards: FabricBinderItem.getModifiers called for " + stack.getItem() + " in slot " + slot.inventory().getSlotType().getName());
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = HashMultimap.create();
        
        ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        java.util.Map<CardStat, Float> statTotals = new java.util.EnumMap<>(CardStat.class);
        for (ItemStack contentStack : contents.nonEmptyItems()) {
            CardData cardData = contentStack.get(ModDataComponents.CARD_DATA);
            if (cardData != null && !cardData.pokemonId().startsWith("player_")) {
                statTotals.merge(cardData.stat(), cardData.statValue(), Float::sum);
            }
        }
        
        for (java.util.Map.Entry<CardStat, Float> entry : statTotals.entrySet()) {
            CardStat stat = entry.getKey();
            float totalValue = entry.getValue();
            Holder<Attribute> attribute = getVanillaAttribute(stat);
            if (attribute != null && totalValue != 0) {
                float val = totalValue * CobblemonCardsConfig.globalStatMultiplier;
                AttributeModifier.Operation operation = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
                if (stat == CardStat.MAX_HEALTH || stat == CardStat.ARMOR || stat == CardStat.LUCK) {
                    operation = AttributeModifier.Operation.ADD_VALUE;
                } else {
                    val /= 100.0f;
                }
                
                ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_" + stat.getSerializedName());
                AttributeModifier modifier = new AttributeModifier(modifierId, val, operation);
                modifiers.put(attribute, modifier);
            }
        }
        
        return modifiers;
    }

    @Override
    public boolean canEquipFromUse(ItemStack stack, LivingEntity entity) {
        return false;
    }
}
