package com.howlite.cobblemoncards.fabric.item;

import com.howlite.cobblemoncards.CobblemonCardsConfig;
import com.howlite.cobblemoncards.component.CardData;
import com.howlite.cobblemoncards.component.CardStat;
import com.howlite.cobblemoncards.component.ModDataComponents;
import com.howlite.cobblemoncards.item.custom.BinderItem;
import com.howlite.cobblemoncards.item.custom.BinderTier;
import io.wispforest.accessories.api.Accessory;
import io.wispforest.accessories.api.attributes.AccessoryAttributeBuilder;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class FabricBinderItem extends BinderItem implements Accessory {

    public FabricBinderItem(BinderTier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public void getDynamicModifiers(ItemStack stack, SlotReference reference, AccessoryAttributeBuilder builder) {
        // Lire les cartes depuis le nouveau composant BINDER_CONTENTS
        List<ItemStack> binderItems = stack.get(ModDataComponents.BINDER_CONTENTS);
        Iterable<ItemStack> contentItems;
        if (binderItems != null) {
            contentItems = binderItems.stream().filter(s -> !s.isEmpty()).toList();
        } else {
            // Fallback sur le composant vanilla CONTAINER pour la rétrocompatibilité
            contentItems = stack.getOrDefault(
                    net.minecraft.core.component.DataComponents.CONTAINER,
                    net.minecraft.world.item.component.ItemContainerContents.EMPTY
            ).nonEmptyItems();
        }

        Map<CardStat, Float> statTotals = new EnumMap<>(CardStat.class);
        for (ItemStack contentStack : contentItems) {
            CardData cardData = contentStack.get(ModDataComponents.CARD_DATA);
            if (cardData != null && !com.howlite.cobblemoncards.util.CardUtil.isCosmeticCard(cardData.pokemonId())) {
                statTotals.merge(cardData.stat(), cardData.statValue(), Float::sum);
            }
        }

        for (Map.Entry<CardStat, Float> entry : statTotals.entrySet()) {
            CardStat stat = entry.getKey();
            float totalValue = entry.getValue();
            Holder<Attribute> attribute = getVanillaAttribute(stat);
            if (attribute != null && totalValue != 0) {
                float val = totalValue * CobblemonCardsConfig.globalStatMultiplier;
                AttributeModifier.Operation operation;
                // ADD_VALUE pour les stats à base nulle ou absolues
                // ADD_MULTIPLIED_BASE pour les stats à base non nulle (vitesse, dégâts...)
                if (stat == CardStat.MAX_HEALTH || stat == CardStat.ARMOR || stat == CardStat.LUCK
                        || stat == CardStat.MINING_SPEED) {
                    operation = AttributeModifier.Operation.ADD_VALUE;
                } else {
                    operation = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
                    val /= 100.0f;
                }

                String path = "binder_modifier_" + reference.slotName() + "_" + reference.slot() + "_" + stat.getSerializedName();
                builder.addExclusive(
                        attribute,
                        ResourceLocation.fromNamespaceAndPath("cobblemon-cards", path),
                        val,
                        operation
                );
            }
        }
    }

    @Override
    public boolean canEquipFromUse(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canEquip(ItemStack stack, SlotReference reference) {
        return reference.slotName().equals("belt");
    }
}
