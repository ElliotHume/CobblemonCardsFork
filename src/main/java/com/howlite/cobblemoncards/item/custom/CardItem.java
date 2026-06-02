package com.howlite.cobblemoncards.item.custom;

import com.howlite.cobblemoncards.CobblemonCardsConfig;
import com.howlite.cobblemoncards.component.CardData;
import com.howlite.cobblemoncards.component.ModDataComponents;
import com.howlite.cobblemoncards.util.ClientAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class CardItem extends Item {

    public CardItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        CardData data = stack.get(ModDataComponents.CARD_DATA);

        if (data != null) {
            String key = data.isShiny() ? "item.cobblemon-cards.card.shiny" : "item.cobblemon-cards.card.normal";
            return Component.translatable(key, getFormattedName(data.pokemonId()))
                    .withStyle(data.isShiny() ? ChatFormatting.GOLD : ChatFormatting.YELLOW);
        }

        return Component.translatable("item.cobblemon-cards.card").withStyle(ChatFormatting.GRAY);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        CardData data = stack.get(ModDataComponents.CARD_DATA);

        if (data != null) {
            if (ClientAccess.isShiftDown()) {
                if (data.pokemonId().startsWith("player_")) {
                    tooltipComponents.add(Component.translatable("tooltip.cobblemon-cards.cosmetic_card").withStyle(ChatFormatting.LIGHT_PURPLE));
                } else {
                    // 1. Statistique (Mise en avant)
                    float realValue = data.statValue() * CobblemonCardsConfig.globalStatMultiplier;
                    String sign = realValue >= 0 ? "+" : "";
                    String formattedValue = String.format("%s%.2f", sign, realValue);
                    tooltipComponents.add(Component.literal(formattedValue + " ").append(data.stat().getTranslatedName())
                            .withStyle(ChatFormatting.GREEN));
                }

                tooltipComponents.add(Component.empty()); // Séparateur

                // 2. Pokémon (Label GRAY | Valeur WHITE)
                tooltipComponents.add(Component.translatable("tooltip.cobblemon-cards.label.pokemon").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(" " + getFormattedName(data.pokemonId())).withStyle(ChatFormatting.WHITE)));

                // 3. Rareté (Label GRAY | Valeur AQUA)
                tooltipComponents.add(Component.translatable("tooltip.cobblemon-cards.label.rarity").withStyle(ChatFormatting.GRAY)
                        .append(Component.translatable("rarity.cobblemon-cards." + data.rarity().toLowerCase()).withStyle(ChatFormatting.AQUA)));

                // 4. Effet Holo (Label GRAY | Valeur LIGHT_PURPLE)
                data.effect().ifPresent(effect -> {
                    tooltipComponents.add(Component.translatable("tooltip.cobblemon-cards.label.holo").withStyle(ChatFormatting.GRAY)
                            .append(Component.translatable("holo.cobblemon-cards." + effect).withStyle(ChatFormatting.LIGHT_PURPLE)));
                });

                // 5. Arrière-plan (Label GRAY | Valeur YELLOW)
                data.background().ifPresent(bg -> {
                    tooltipComponents.add(Component.translatable("tooltip.cobblemon-cards.label.background").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(" " + capitalize(bg)).withStyle(ChatFormatting.YELLOW)));
                });

                // 6. Shiny (Label GRAY | Valeur GOLD BOLD)
                Component shinyText = data.isShiny() 
                    ? Component.translatable("tooltip.cobblemon-cards.yes").withStyle(style -> style.withColor(ChatFormatting.GOLD).withBold(true))
                    : Component.translatable("tooltip.cobblemon-cards.no").withStyle(ChatFormatting.GRAY);

                tooltipComponents.add(Component.translatable("tooltip.cobblemon-cards.label.shiny").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(" ")).append(shinyText));

                // 7. Grade (Label GRAY | Valeur WHITE)
                if (data.grade() > 0) {
                    tooltipComponents.add(Component.translatable("tooltip.cobblemon-cards.label.grade").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(" " + data.grade()).withStyle(ChatFormatting.WHITE)));
                }
            } else {
                // Info Shift (DARK_GRAY)
                tooltipComponents.add(Component.translatable("tooltip.cobblemon-cards.press_shift")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    private String getFormattedName(String id) {
        if (id == null) return "";
        if (id.startsWith("player_")) {
            String name = id.substring("player_".length());
            int underscoreIdx = name.indexOf('_');
            if (underscoreIdx != -1) {
                name = name.substring(underscoreIdx + 1);
            }
            if (name.isEmpty()) return "";
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        return capitalize(id);
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}