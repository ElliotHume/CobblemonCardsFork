package com.howlite.cobblemoncards.client;

import com.howlite.cobblemoncards.block.entity.ModBlockEntities;
import com.howlite.cobblemoncards.client.render.AdvancedHoloProjectorBlockEntityRenderer;
import com.howlite.cobblemoncards.client.render.CardCabinetBlockEntityRenderer;
import com.howlite.cobblemoncards.client.render.GradingStationBlockEntityRenderer;
import com.howlite.cobblemoncards.client.render.HoloProjectorBlockEntityRenderer;
import com.howlite.cobblemoncards.menu.ModMenuTypes;
import com.howlite.cobblemoncards.screen.AdvancedHoloProjectorScreen;
import com.howlite.cobblemoncards.screen.BinderScreen;
import com.howlite.cobblemoncards.screen.CardCabinetScreen;
import com.howlite.cobblemoncards.screen.CardRecyclerScreen;
import com.howlite.cobblemoncards.screen.CardRestorerScreen;
import com.howlite.cobblemoncards.util.ClientAccess;
import com.howlite.cobblemoncards.util.PlatformHelper;
import com.howlite.cobblemoncards.client.render.MiniHoloProjectorBlockEntityRenderer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class CobblemonCardsClient {
    
    public static void init() {
        // Enregistre le provider pour la touche Shift de manière sécurisée (Split Environments)
        ClientAccess.setShiftKeyProvider(Screen::hasShiftDown);

        // ENREGISTREMENT DES ECRANS (Fabric uniquement, NeoForge utilise RegisterMenuScreensEvent)
        if (!PlatformHelper.INSTANCE.isNeoForge()) {
            MenuScreens.register(ModMenuTypes.BINDER_MENU, BinderScreen::new);
            MenuScreens.register(ModMenuTypes.CARD_RECYCLER_MENU, CardRecyclerScreen::new);
            MenuScreens.register(ModMenuTypes.CARD_RESTORER_MENU, CardRestorerScreen::new);
            MenuScreens.register(ModMenuTypes.ADVANCED_HOLO_PROJECTOR_MENU, AdvancedHoloProjectorScreen::new);
            MenuScreens.register(ModMenuTypes.CARD_CABINET_MENU, CardCabinetScreen::new);
        }

        // ENREGISTREMENT DES RENDERERS DE BLOCS
        BlockEntityRenderers.register(ModBlockEntities.GRADING_STATION_BE, GradingStationBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.HOLO_PROJECTOR_BE, HoloProjectorBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.ADVANCED_HOLO_PROJECTOR_BE, AdvancedHoloProjectorBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.MINI_HOLO_PROJECTOR_BE, MiniHoloProjectorBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.CARD_CABINET_BE, CardCabinetBlockEntityRenderer::new);
    }
}
