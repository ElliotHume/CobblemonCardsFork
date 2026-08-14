package com.howlite.cobblemoncards.menu;

import com.howlite.cobblemoncards.CobblemonCards;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

import com.howlite.cobblemoncards.util.PlatformHelper;

public class ModMenuTypes {

    public static final MenuType<BinderMenu> BINDER_MENU = Registry.register(
            BuiltInRegistries.MENU,
            ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, "binder_menu"),
            PlatformHelper.INSTANCE.createBinderMenuType()
    );

    public static final MenuType<CardRecyclerMenu> CARD_RECYCLER_MENU = Registry.register(
            BuiltInRegistries.MENU,
            ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, "card_recycler_menu"),
            new MenuType<>(CardRecyclerMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );

    public static final MenuType<CardRestorerMenu> CARD_RESTORER_MENU = Registry.register(
            BuiltInRegistries.MENU,
            ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, "card_restorer_menu"),
            new MenuType<>(CardRestorerMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );

    public static final MenuType<AdvancedHoloProjectorMenu> ADVANCED_HOLO_PROJECTOR_MENU = Registry.register(
            BuiltInRegistries.MENU,
            ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, "advanced_holo_projector_menu"),
            new MenuType<>(AdvancedHoloProjectorMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );

    public static final MenuType<CardCabinetMenu> CARD_CABINET_MENU = Registry.register(
            BuiltInRegistries.MENU,
            ResourceLocation.fromNamespaceAndPath(CobblemonCards.MOD_ID, "card_cabinet_menu"),
            new MenuType<>(CardCabinetMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );

    public static void register() {
        CobblemonCards.LOGGER.info("Registering Menu Types for " + CobblemonCards.MOD_ID);
    }
}
