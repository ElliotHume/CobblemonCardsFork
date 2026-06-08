package com.howlite.cobblemoncards.fabric.item;

import com.howlite.cobblemoncards.item.custom.MasterAlbumItem;
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
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class FabricMasterAlbumItem extends MasterAlbumItem implements Trinket {

    public FabricMasterAlbumItem(Properties properties) {
        super(properties);
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, ResourceLocation id) {
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = HashMultimap.create();

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

                        AttributeModifier modifier = new AttributeModifier(
                            ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "album_modifier_" + stat.getSerializedName() + "_" + index),
                            val,
                            operation
                        );
                        modifiers.put(attribute, modifier);
                        index++;
                    }
                }
            }
        }

        return modifiers;
    }
}
