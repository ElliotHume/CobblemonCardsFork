package com.howlite.cobblemoncards.item.custom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.howlite.cobblemoncards.CobblemonCardsConfig;
import com.howlite.cobblemoncards.component.CardData;
import com.howlite.cobblemoncards.component.CardStat;
import com.howlite.cobblemoncards.component.ModDataComponents;
import com.howlite.cobblemoncards.menu.BinderMenu;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class BinderItem extends TrinketItem {
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
            if (TrinketItem.equipItem(player, itemStack)) {
                return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
            }
            return InteractionResultHolder.pass(itemStack);
        }

        // Bruit d'ouverture (On utilise le son du livre pour faire "classeur")
        level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 0.8f);

        if (!level.isClientSide()) {
            player.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, p) -> new BinderMenu(containerId, playerInventory, itemStack),
                    itemStack.getHoverName()
            ));
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    private Holder<Attribute> getVanillaAttribute(CardStat stat) {
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

        ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        Map<CardStat, Float> statTotals = new EnumMap<>(CardStat.class);

        for (ItemStack contentStack : contents.nonEmptyItems()) {
            CardData cardData = contentStack.get(ModDataComponents.CARD_DATA);
            if (cardData != null && !cardData.pokemonId().startsWith("player_")) {
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

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, ResourceLocation id) {
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = HashMultimap.create();

        ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        Map<CardStat, Float> statTotals = new EnumMap<>(CardStat.class);

        for (ItemStack contentStack : contents.nonEmptyItems()) {
            CardData cardData = contentStack.get(ModDataComponents.CARD_DATA);
            if (cardData != null && !cardData.pokemonId().startsWith("player_")) {
                statTotals.merge(cardData.stat(), cardData.statValue(), Float::sum);
            }
        }

        for (Map.Entry<CardStat, Float> entry : statTotals.entrySet()) {
            CardStat stat = entry.getKey();
            float totalValue = entry.getValue();
            Holder<Attribute> attribute = getVanillaAttribute(stat);

            if (attribute != null && totalValue > 0) {
                double finalValue = totalValue * CobblemonCardsConfig.globalStatMultiplier;

                // --- Caps par stat pour éviter le "broken stacking" ---
                // MOVEMENT_SPEED / ATTACK_SPEED : operation ADD_MULTIPLIED_BASE
                //   base speed = 0.1, donc +0.3 = +300% de vitesse → cap à 0.5 (soit +50% de la base)
                // ATTACK_DAMAGE : valeurs absolues. Cap à +4.0 (≈ 2 épées de fer bonus)
                // MAX_HEALTH : cap à +10 hearts (20 PV bonus, soit +50% de la vie de base)
                // ARMOR : cap à +10 (armure de diamant = 20, donc +10 = +50%)
                // MINING_SPEED : cap à +4.0 (raisonnable, rend le minage rapide sans trivial)
                // LUCK : cap à +10.0
                finalValue = switch (stat) {
                    case MOVEMENT_SPEED, ATTACK_SPEED -> Math.min(finalValue, 0.5);
                    case ATTACK_DAMAGE               -> Math.min(finalValue, 4.0);
                    case MAX_HEALTH                  -> Math.min(finalValue, 10.0);
                    case ARMOR                       -> Math.min(finalValue, 10.0);
                    case MINING_SPEED                -> Math.min(finalValue, 4.0);
                    case LUCK                        -> Math.min(finalValue, 10.0);
                    default                          -> finalValue;
                };

                AttributeModifier.Operation operation = (stat == CardStat.MOVEMENT_SPEED || stat == CardStat.ATTACK_SPEED)
                        ? AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                        : AttributeModifier.Operation.ADD_VALUE;

                ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "binder_bonus_" + stat.getSerializedName());
                modifiers.put(attribute, new AttributeModifier(modifierId, finalValue, operation));
            }
        }

        return modifiers;
    }
}
