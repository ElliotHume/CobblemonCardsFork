package com.howlite.cobblemoncards.item.custom;

import com.howlite.cobblemoncards.network.SyncDiscoveredCardsPayload;
import com.howlite.cobblemoncards.util.PlatformHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.List;

public class CardDexItem extends Item {
    public CardDexItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            // First force-run inventory scan to make sure collection is 100% up-to-date!
            com.howlite.cobblemoncards.util.CardAdvancementManager.checkAdvancements(serverPlayer);

            List<String> discovered = PlatformHelper.INSTANCE.getDiscoveredCards(serverPlayer);
            
            // Play a cool book page turn sound for immersion
            level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 1.0f);

            // Send payload to open client screen with synced discovered IDs
            PlatformHelper.INSTANCE.sendToPlayer(serverPlayer, new SyncDiscoveredCardsPayload(discovered));
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}
