package com.howlite.cobblemoncards;

import eu.midnightdust.lib.config.MidnightConfig;

public class CobblemonCardsConfig extends MidnightConfig {
    @Entry(min = 0.1f, max = 100.0f)
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
}