package com.howlite.cobblemoncards.neoforge.item;

import com.howlite.cobblemoncards.item.custom.BinderItem;
import com.howlite.cobblemoncards.item.custom.BinderTier;
import com.howlite.cobblemoncards.component.CardData;
import com.howlite.cobblemoncards.component.CardStat;
import com.howlite.cobblemoncards.component.ModDataComponents;
import io.wispforest.accessories.api.Accessory;
import io.wispforest.accessories.api.attributes.AccessoryAttributeBuilder;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("null")
public class NeoForgeBinderItem extends BinderItem implements Accessory {

    public NeoForgeBinderItem(BinderTier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public void getDynamicModifiers(ItemStack stack, SlotReference reference, AccessoryAttributeBuilder builder) {
        // Lire les cartes depuis BINDER_CONTENTS (nouveau stockage custom)
        List<ItemStack> binderItems = stack.get(ModDataComponents.BINDER_CONTENTS);
        Iterable<ItemStack> contentItems;
        if (binderItems != null) {
            contentItems = binderItems.stream().filter(s -> !s.isEmpty()).toList();
        } else {
            // Fallback sur CONTAINER pour la rétrocompatibilité des anciennes saves
            contentItems = stack.getOrDefault(DataComponents.CONTAINER, net.minecraft.world.item.component.ItemContainerContents.EMPTY).nonEmptyItems();
        }

        Map<CardStat, Float> statTotals = new EnumMap<>(CardStat.class);
        for (ItemStack contentStack : contentItems) {
            CardData cardData = contentStack.get(ModDataComponents.CARD_DATA);
            if (cardData != null && !com.howlite.cobblemoncards.util.CardUtil.isCosmeticCard(cardData.pokemonId())) {
                statTotals.merge(cardData.stat(), cardData.statValue(), Float::sum);
            }
        }

        for (java.util.Map.Entry<CardStat, Float> entry : statTotals.entrySet()) {
            CardStat stat = entry.getKey();
            float totalValue = entry.getValue();
            Holder<Attribute> attribute = getVanillaAttribute(stat);
            if (attribute != null && totalValue != 0) {
                // Config multiplier + application mode (flat vs percent) resolved in one place.
                float val = com.howlite.cobblemoncards.util.CardStatUtil.getAttributeModifierValue(stat, totalValue);
                if (val == 0.0f) continue;
                AttributeModifier.Operation operation =
                        com.howlite.cobblemoncards.util.CardStatUtil.getAttributeOperation(stat);

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
        return reference.slotName().equals("binder");
    }
}
