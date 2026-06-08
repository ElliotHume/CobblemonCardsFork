package com.howlite.cobblemoncards.item.custom;

import com.howlite.cobblemoncards.util.PlatformHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GodPackTicketItem extends Item {
    public GodPackTicketItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);

        if (!level.isClientSide()) {
            boolean hasGuarantee = PlatformHelper.INSTANCE.hasGuaranteedGodPack(player);

            if (hasGuarantee) {
                player.displayClientMessage(Component.translatable("message.cobblemon-cards.god_pack_ticket.already_active").withStyle(ChatFormatting.RED), true);
                return InteractionResultHolder.fail(itemStack);
            }

            // Activer la garantie
            PlatformHelper.INSTANCE.setGuaranteedGodPack(player, true);

            // Effets visuels et sonores
            level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.0f);
            
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT, 
                    player.getX(), player.getY() + 1, player.getZ(), 
                    20, 0.5, 0.5, 0.5, 0.1);
            }

            player.displayClientMessage(Component.translatable("message.cobblemon-cards.god_pack_ticket.activated").withStyle(ChatFormatting.GOLD), false);

            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    @Override
    public void appendHoverText(net.minecraft.world.item.ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, java.util.List<net.minecraft.network.chat.Component> tooltipComponents, net.minecraft.world.item.TooltipFlag tooltipFlag) {
        tooltipComponents.add(net.minecraft.network.chat.Component.translatable("tooltip.cobblemon-cards.god_pack_ticket.description").withStyle(net.minecraft.ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
