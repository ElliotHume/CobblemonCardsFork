package com.howlite.cobblemoncards.manager;

import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BoosterPackManager {
    // On garde en mémoire les cartes générées pour chaque joueur
    private static final ConcurrentHashMap<UUID, List<ItemStack>> PENDING_REWARDS = new ConcurrentHashMap<>();

    public static void addPendingRewards(UUID playerUUID, List<ItemStack> rewards) {
        PENDING_REWARDS.put(playerUUID, rewards);
    }

    public static List<ItemStack> getAndClearRewards(UUID playerUUID) {
        return PENDING_REWARDS.remove(playerUUID);
    }
}