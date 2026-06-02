package com.howlite.cobblemoncards.item.custom;

import com.howlite.cobblemoncards.component.DiskData;
import com.howlite.cobblemoncards.component.ModDataComponents;
import com.howlite.cobblemoncards.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class CardStructureDiskItem extends Item {

    private static final int MAX_DUST = 1000;

    public CardStructureDiskItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack heldStack = player.getItemInHand(usedHand);
        DiskData data = heldStack.getOrDefault(ModDataComponents.DISK_DATA, DiskData.empty());

        if (!level.isClientSide) {
            if (data.dust() >= MAX_DUST) {
                player.displayClientMessage(Component.translatable("message.cobblemon-cards.structure_disk.disk_full").withStyle(ChatFormatting.RED), true);
                return InteractionResultHolder.fail(heldStack);
            }

            int dustSlot = findDustSlot(player);

            if (dustSlot != -1) {
                ItemStack dustStack = player.getInventory().getItem(dustSlot);
                
                int dustToConsume = 1;
                // Si le joueur accroupi (sneak), on essaie de consommer tout le stack d'un coup (jusqu'à la limite du disque)
                if (player.isShiftKeyDown()) {
                    int spaceLeft = MAX_DUST - data.dust();
                    dustToConsume = Math.min(dustStack.getCount(), spaceLeft);
                }

                dustStack.shrink(dustToConsume);

                heldStack.set(ModDataComponents.DISK_DATA, new DiskData(
                        data.dust() + dustToConsume,
                        data.scanCount(),
                        data.scannedPokemon(),
                        data.targetSpecies()
                ));

                level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 1.2f);
                return InteractionResultHolder.success(heldStack);
            } else {
                player.displayClientMessage(Component.translatable("message.cobblemon-cards.structure_disk.no_dust").withStyle(ChatFormatting.RED), true);
            }
        }

        return InteractionResultHolder.pass(heldStack);
    }

    private int findDustSlot(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(ModItems.CARD_DUST)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        DiskData data = stack.getOrDefault(ModDataComponents.DISK_DATA, DiskData.empty());
        return data.dust() > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        DiskData data = stack.getOrDefault(ModDataComponents.DISK_DATA, DiskData.empty());
        return Math.round(13.0f * ((float) data.dust() / MAX_DUST));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        // Couleur violette
        return Mth.color(160, 32, 240);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        DiskData data = stack.getOrDefault(ModDataComponents.DISK_DATA, DiskData.empty());
        
        int dust = data.dust();
        
        tooltipComponents.add(Component.translatable("tooltip.cobblemon-cards.structure_disk.charge_instruction").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.translatable("tooltip.cobblemon-cards.structure_disk.charge_all_instruction").withStyle(ChatFormatting.YELLOW));
        
        tooltipComponents.add(Component.empty());
        
        tooltipComponents.add(Component.translatable("tooltip.cobblemon-cards.structure_disk.accumulated_dust").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(dust + " / " + MAX_DUST).withStyle(ChatFormatting.AQUA)));
                
        String potentialRarity = "Common";
        ChatFormatting rarityColor = ChatFormatting.WHITE;
        
        if (dust >= 1000) {
            potentialRarity = "Legendary";
            rarityColor = ChatFormatting.LIGHT_PURPLE;
        } else if (dust >= 500) {
            potentialRarity = "Epic";
            rarityColor = ChatFormatting.GOLD;
        } else if (dust >= 200) {
            potentialRarity = "Rare";
            rarityColor = ChatFormatting.DARK_PURPLE;
        } else if (dust >= 50) {
            potentialRarity = "Uncommon";
            rarityColor = ChatFormatting.BLUE;
        }
        
        tooltipComponents.add(Component.translatable("tooltip.cobblemon-cards.structure_disk.potential_rarity").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(potentialRarity).withStyle(rarityColor)));

        if (data.scanCount() > 0) {
            tooltipComponents.add(Component.empty());

            data.targetSpecies().ifPresent(species -> {
                tooltipComponents.add(Component.translatable("tooltip.cobblemon-cards.structure_disk.target_species").withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal(species).withStyle(ChatFormatting.YELLOW)));
            });

            int count = data.scanCount();
            StringBuilder progress = new StringBuilder(" [");
            for (int i = 0; i < 5; i++) {
                if (i < count) {
                    progress.append("■");
                } else {
                    progress.append("□");
                }
            }
            progress.append("] ").append(count).append("/5");

            tooltipComponents.add(Component.translatable("tooltip.cobblemon-cards.structure_disk.scan_progress").withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(progress.toString()).withStyle(ChatFormatting.AQUA)));
        }
                
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}