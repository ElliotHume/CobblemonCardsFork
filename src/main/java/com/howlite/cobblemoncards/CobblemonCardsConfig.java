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
}