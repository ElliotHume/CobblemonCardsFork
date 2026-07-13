package com.howlite.cobblemoncards.item.custom;

import com.howlite.cobblemoncards.CobblemonCardsConfig;
import com.howlite.cobblemoncards.component.CardData;
import com.howlite.cobblemoncards.component.CardStat;
import com.howlite.cobblemoncards.component.ModDataComponents;
import com.howlite.cobblemoncards.util.PlatformHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.howlite.cobblemoncards.util.BinderLocator;
import net.minecraft.server.level.ServerPlayer;

public class BinderItem extends Item {
    private final BinderTier tier;

    public BinderItem(BinderTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public BinderTier getTier() {
        return tier;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (PlatformHelper.INSTANCE.equipItem(player, itemStack)) {
                return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
            }
            return InteractionResultHolder.pass(itemStack);
        }

        // Bruit d'ouverture (On utilise le son du livre pour faire "classeur")
        level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 0.8f);

        if (!level.isClientSide()) {
            PlatformHelper.INSTANCE.openBinderMenu((ServerPlayer) player, BinderLocator.hand(hand), itemStack.getHoverName());
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    public Holder<Attribute> getVanillaAttribute(CardStat stat) {
        return switch (stat) {
            case MINING_SPEED -> Attributes.MINING_EFFICIENCY;
            case MOVEMENT_SPEED -> Attributes.MOVEMENT_SPEED;
            case ATTACK_DAMAGE -> Attributes.ATTACK_DAMAGE;
            case ATTACK_SPEED -> Attributes.ATTACK_SPEED;
            case LUCK -> Attributes.LUCK;
            case ARMOR -> Attributes.ARMOR;
            case MAX_HEALTH -> Attributes.MAX_HEALTH;
            default -> null;
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        // Affichage du nombre de pages
        tooltipComponents.add(Component.translatable("gui.cobblemon-cards.binder.pages", tier.getPages()).withStyle(ChatFormatting.GRAY));

        // Read from BINDER_CONTENTS (new). Fall back to vanilla CONTAINER for unmigrated saves.
        List<ItemStack> binderItems = stack.get(ModDataComponents.BINDER_CONTENTS);
        Iterable<ItemStack> contentItems;
        if (binderItems != null) {
            contentItems = binderItems.stream().filter(s -> !s.isEmpty()).toList();
        } else {
            contentItems = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).nonEmptyItems();
        }
        Map<CardStat, Float> statTotals = new EnumMap<>(CardStat.class);

        for (ItemStack contentStack : contentItems) {
            CardData cardData = contentStack.get(ModDataComponents.CARD_DATA);
            if (cardData != null && !com.howlite.cobblemoncards.util.CardUtil.isCosmeticCard(cardData.pokemonId())) {
                statTotals.merge(cardData.stat(), cardData.statValue(), Float::sum);
            }
        }

        boolean hasHeader = false;
        for (Map.Entry<CardStat, Float> entry : statTotals.entrySet()) {
            CardStat stat = entry.getKey();
            float totalValue = entry.getValue();

            if (getVanillaAttribute(stat) == null && totalValue > 0) {
                if (!hasHeader) {
                    tooltipComponents.add(Component.empty());
                    tooltipComponents.add(Component.translatable("gui.cobblemon-cards.binder.stats_bonus").withStyle(ChatFormatting.GRAY));
                    hasHeader = true;
                }

                float finalValue = totalValue * CobblemonCardsConfig.globalStatMultiplier;
                String formattedValue = String.format("+%.1f%%", finalValue);

                tooltipComponents.add(Component.literal(formattedValue + " ").append(stat.getTranslatedName())
                        .withStyle(ChatFormatting.AQUA));
            }
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
