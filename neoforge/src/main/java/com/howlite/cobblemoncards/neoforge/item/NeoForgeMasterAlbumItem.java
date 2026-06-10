package com.howlite.cobblemoncards.neoforge.item;

import com.howlite.cobblemoncards.item.custom.MasterAlbumItem;
import com.howlite.cobblemoncards.component.CardData;
import com.howlite.cobblemoncards.component.CardStat;
import com.howlite.cobblemoncards.component.ModDataComponents;
import com.howlite.cobblemoncards.CobblemonCardsConfig;
import io.wispforest.accessories.api.Accessory;
import io.wispforest.accessories.api.attributes.AccessoryAttributeBuilder;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("null")
public class NeoForgeMasterAlbumItem extends MasterAlbumItem implements Accessory {

    public NeoForgeMasterAlbumItem(Properties properties) {
        super(properties);
    }

    @Override
    public void getDynamicModifiers(ItemStack stack, SlotReference reference, AccessoryAttributeBuilder builder) {
        ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        java.util.Map<CardStat, Float> statTotals = new java.util.EnumMap<>(CardStat.class);
        for (ItemStack contentStack : contents.nonEmptyItems()) {
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
                float val = totalValue * CobblemonCardsConfig.globalStatMultiplier;
                AttributeModifier.Operation operation = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
                if (stat == CardStat.MAX_HEALTH || stat == CardStat.ARMOR || stat == CardStat.LUCK) {
                    operation = AttributeModifier.Operation.ADD_VALUE;
                } else {
                    val /= 100.0f;
                }

                String path = "album_modifier_" + reference.slotName() + "_" + reference.slot() + "_" + stat.getSerializedName();
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
