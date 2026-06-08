package com.howlite.cobblemoncards;

import com.howlite.cobblemoncards.block.ModBlocks;
import com.howlite.cobblemoncards.block.entity.ModBlockEntities;
import com.howlite.cobblemoncards.component.ModDataComponents;
import com.howlite.cobblemoncards.event.ModEvents;
import com.howlite.cobblemoncards.item.ModCreativeTabs;
import com.howlite.cobblemoncards.item.ModItems;
import eu.midnightdust.lib.config.MidnightConfig;
import org.slf4j.Logger;
import com.howlite.cobblemoncards.sound.ModSounds;
import com.howlite.cobblemoncards.menu.ModMenuTypes;
import org.slf4j.LoggerFactory;

public class CobblemonCards {
    public static final String MOD_ID = "cobblemon-cards";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        // Initialisation de la configuration MidnightLib
        MidnightConfig.init(MOD_ID, CobblemonCardsConfig.class);

        ModDataComponents.register();
        ModItems.register();
        ModBlocks.register();
        ModBlockEntities.register();
        ModMenuTypes.register();
        ModCreativeTabs.register();
        ModEvents.registerEvents();
        ModSounds.registerSounds();
    }
}
