package com.howlite.cobblemoncards.neoforge.item;

import com.howlite.cobblemoncards.item.custom.BinderItem;
import com.howlite.cobblemoncards.item.custom.BinderTier;
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

public class NeoForgeBinderItem extends BinderItem implements Accessory {

    public NeoForgeBinderItem(BinderTier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public void getDynamicModifiers(ItemStack stack, SlotReference reference, AccessoryAttributeBuilder builder) {
        ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        int index = 0;
        for (ItemStack contentStack : contents.nonEmptyItems()) {
            CardData cardData = contentStack.get(ModDataComponents.CARD_DATA);
            if (cardData != null && !cardData.pokemonId().startsWith("player_")) {
                CardStat stat = cardData.stat();
                Holder<Attribute> attribute = getVanillaAttribute(stat);
                if (attribute != null) {
                    float val = cardData.statValue() * CobblemonCardsConfig.globalStatMultiplier;
                    if (val != 0) {
                        AttributeModifier.Operation operation = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
                        if (stat == CardStat.MAX_HEALTH || stat == CardStat.ARMOR) {
                            operation = AttributeModifier.Operation.ADD_VALUE;
                        }

                        builder.addExclusive(
                            attribute,
                            ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "binder_modifier_" + stat.getSerializedName() + "_" + index),
                            val,
                            operation
                        );
                        index++;
                    }
                }
            }
        }
    }
}
