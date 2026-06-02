package com.howlite.cobblemoncards.item.custom;

import com.howlite.cobblemoncards.attachment.PlayerDataAttachments;
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

public class GodPackEssenceItem extends Item {
    public GodPackEssenceItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);

        if (!level.isClientSide()) {
            boolean hasGuarantee = player.getAttachedOrCreate(PlayerDataAttachments.HAS_GUARANTEED_GOD_PACK, () -> false);

            if (hasGuarantee) {
                player.displayClientMessage(Component.literal("§cVous avez déjà une chance légendaire en attente !"), true);
                return InteractionResultHolder.fail(itemStack);
            }

            // Activer la garantie
            player.setAttached(PlayerDataAttachments.HAS_GUARANTEED_GOD_PACK, true);

            // Effets visuels et sonores
            level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.0f);
            
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT, 
                    player.getX(), player.getY() + 1, player.getZ(), 
                    20, 0.5, 0.5, 0.5, 0.1);
            }

            player.displayClientMessage(Component.literal("§6Votre prochain booster sera légendaire..."), false);

            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}
