package com.howlite.cobblemoncards.item.custom;

import com.howlite.cobblemoncards.CobblemonCardsConfig;
import com.howlite.cobblemoncards.attachment.PlayerDataAttachments;
import com.howlite.cobblemoncards.item.custom.loot.BoosterLootTable;
import com.howlite.cobblemoncards.manager.BoosterPackManager;
import com.howlite.cobblemoncards.network.GiveRewardPayload;
import com.howlite.cobblemoncards.network.OpenBoosterPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.howlite.cobblemoncards.component.ModDataComponents;

import java.util.ArrayList;
import java.util.List;

public class BoosterPackItem extends Item {
    private final String boosterType; // "base", "gen1", "gen2", "fire", "water", "grass"

    public BoosterPackItem(Properties properties) {
        this("base", properties);
    }

    public BoosterPackItem(String boosterType, Properties properties) {
        super(properties);
        this.boosterType = boosterType;
    }

    public String getBoosterType() {
        return boosterType;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);

        if (!level.isClientSide()) {
            List<ItemStack> serverRewards = new ArrayList<>();
            List<ItemStack> displayRewards = new ArrayList<>();

            List<ItemStack> customRewards = itemStack.get(ModDataComponents.CUSTOM_BOOSTER_DATA);
            boolean isCustom = customRewards != null && !customRewards.isEmpty();

            if (isCustom) {
                for (ItemStack reward : customRewards) {
                    serverRewards.add(reward.copy());
                    displayRewards.add(reward.copy());
                }
            } else {
                // 1. Détermination si c'est un God Pack
                boolean naturalGodPack = level.random.nextFloat() * 100f <= CobblemonCardsConfig.godPackTicketChance;
                boolean guaranteedGodPack = player.getAttachedOrCreate(PlayerDataAttachments.HAS_GUARANTEED_GOD_PACK, () -> false);
                boolean isGodPack = naturalGodPack || guaranteedGodPack;

                if (isGodPack) {
                    // Son du tonnerre pour le God Pack
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                        SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0f, 1.0f);
                    
                    // On génère 5 cartes épiques/légendaires shiny
                    for (int i = 0; i < 5; i++) {
                        ItemStack card = BoosterLootTable.getGodPackReward(this.boosterType);
                        serverRewards.add(card.copy());
                        displayRewards.add(card.copy());
                    }

                    // Consommation de la garantie si utilisée
                    if (guaranteedGodPack) {
                        player.setAttached(PlayerDataAttachments.HAS_GUARANTEED_GOD_PACK, false);
                    }
                } else {
                    // Logique classique
                    for (int i = 0; i < 3; i++) {
                        ItemStack card = BoosterLootTable.getRandomReward("common", this.boosterType);
                        serverRewards.add(card.copy());
                        displayRewards.add(card.copy());
                    }

                    ItemStack uncommon = BoosterLootTable.getRandomReward("uncommon", this.boosterType);
                    serverRewards.add(uncommon.copy());
                    displayRewards.add(uncommon.copy());

                    ItemStack rare = BoosterLootTable.getRandomRewardAbove("rare", this.boosterType);
                    serverRewards.add(rare.copy());
                    displayRewards.add(rare.copy());
                }
            }

            // On garde les cartes sur le serveur
            BoosterPackManager.addPendingRewards(player.getUUID(), serverRewards);

            // On ouvre l'écran et on envoie les visuels
            ServerPlayNetworking.send((ServerPlayer) player, new OpenBoosterPayload());
            ServerPlayNetworking.send((ServerPlayer) player, new GiveRewardPayload(displayRewards));

            if (player instanceof ServerPlayer serverPlayer) {
                com.howlite.cobblemoncards.util.CardAdvancementManager.trackBoosterOpening(serverPlayer);
            }

            // On consomme le booster
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    @Override
    public void appendHoverText(net.minecraft.world.item.ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, java.util.List<net.minecraft.network.chat.Component> tooltipComponents, net.minecraft.world.item.TooltipFlag tooltipFlag) {
        if (stack.has(ModDataComponents.CUSTOM_BOOSTER_DATA)) {
            tooltipComponents.add(net.minecraft.network.chat.Component.translatable("tooltip.cobblemon-cards.custom_booster_pack.description").withStyle(net.minecraft.ChatFormatting.GRAY));
        } else {
            tooltipComponents.add(net.minecraft.network.chat.Component.translatable("tooltip.cobblemon-cards.booster_pack." + this.boosterType + ".description").withStyle(net.minecraft.ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
