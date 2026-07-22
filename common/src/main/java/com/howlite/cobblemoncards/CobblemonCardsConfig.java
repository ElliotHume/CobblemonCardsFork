package com.howlite.cobblemoncards;

import com.howlite.cobblemoncards.component.CardStat;
import eu.midnightdust.lib.config.MidnightConfig;

public class CobblemonCardsConfig extends MidnightConfig {
    @Entry
    public static boolean enableCardStats = true;

    @Entry
    public static boolean enablePlayerStats = true;

    @Entry
    public static boolean enableSpawnBoostStats = true;

    @Entry(min = 0.0f, max = 100.0f)
    public static float globalStatMultiplier = 10.0f;

    @Entry(min = 1, max = 1200)
    public static int recyclerProcessTime = 40;

    @Entry(min = 0.0f, max = 100.0f)
    public static float godPackTicketChance = 1.0f;

    @Entry(min = 1, max = 12000)
    public static int gradingStationProcessTime = 100;
    
    @Entry(min = 0.0f, max = 100.0f)
    public static float cardDropChance = 1.0f;

    @Entry(min = 0, max = 64)
    public static int gradingStationDustCost = 5;

    @Entry
    public static boolean enableBoosterChestSpawn = true;

    @Entry(min = 0.0f, max = 100.0f)
    public static float boosterChestSpawnChance = 2.0f;

    /**
     * When false (default), species whose National Pokédex number is outside [1, 1025]
     * (i.e. Fakemon added by addon mods) are excluded from card drops and booster packs.
     * Set to true to allow cards for any registered Cobblemon species.
     */
    @Entry
    public static boolean allowFakemonCards = false;

    public static float getStatMultiplier(CardStat stat) {
        if (!enableCardStats) {
            return 0.0f;
        }
        if (stat == null) {
            return globalStatMultiplier;
        }
        if (!enablePlayerStats && isPlayerStat(stat)) {
            return 0.0f;
        }
        if (!enableSpawnBoostStats && isSpawnStat(stat)) {
            return 0.0f;
        }
        return globalStatMultiplier;
    }

    public static boolean isPlayerStat(CardStat stat) {
        return stat == CardStat.MINING_SPEED || stat == CardStat.MOVEMENT_SPEED
                || stat == CardStat.ATTACK_DAMAGE || stat == CardStat.ATTACK_SPEED
                || stat == CardStat.LUCK || stat == CardStat.ARMOR
                || stat == CardStat.MAX_HEALTH || stat == CardStat.CARD_DROP_CHANCE;
    }

    public static boolean isSpawnStat(CardStat stat) {
        return stat != null && stat.getSerializedName().endsWith("_spawn");
    }
}