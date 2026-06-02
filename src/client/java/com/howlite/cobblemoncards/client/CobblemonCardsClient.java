package com.howlite.cobblemoncards.client;

import com.howlite.cobblemoncards.CobblemonCardsConfig;
import com.howlite.cobblemoncards.block.ModBlocks;
import com.howlite.cobblemoncards.block.entity.ModBlockEntities;
import com.howlite.cobblemoncards.client.render.AdvancedHoloProjectorBlockEntityRenderer;
import com.howlite.cobblemoncards.client.render.CardCabinetBlockEntityRenderer;
import com.howlite.cobblemoncards.client.render.GradingStationBlockEntityRenderer;
import com.howlite.cobblemoncards.client.render.HoloProjectorBlockEntityRenderer;
import com.howlite.cobblemoncards.item.ModItems;
import com.howlite.cobblemoncards.menu.ModMenuTypes;
import com.howlite.cobblemoncards.network.GiveRewardPayload;
import com.howlite.cobblemoncards.network.OpenBinderPayload;
import com.howlite.cobblemoncards.network.OpenBoosterPayload;
import com.howlite.cobblemoncards.network.OpenWorkshopPayload;
import com.howlite.cobblemoncards.network.SyncDiscoveredCardsPayload;
import com.howlite.cobblemoncards.render.CardItemRenderer;
import com.howlite.cobblemoncards.screen.AdvancedHoloProjectorScreen;
import com.howlite.cobblemoncards.screen.BinderScreen;
import com.howlite.cobblemoncards.screen.BoosterPackScreen;
import com.howlite.cobblemoncards.screen.CardCabinetScreen;
import com.howlite.cobblemoncards.screen.CardRecyclerScreen;
import com.howlite.cobblemoncards.util.ClientAccess;
import com.mojang.blaze3d.platform.InputConstants;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import org.lwjgl.glfw.GLFW;

public class CobblemonCardsClient implements ClientModInitializer {
    private static KeyMapping openBinderKey;

    @Override
    public void onInitializeClient() {

        // Enregistre les shaders custom
        com.howlite.cobblemoncards.render.ModShaders.register();

        // MidnightLib config screen registration for modmenu - SEULEMENT COTE CLIENT POUR L'UI !
        MidnightConfig.init("cobblemon-cards", CobblemonCardsConfig.class);

        // Enregistre le provider pour la touche Shift de manière sécurisée (Split Environments)
        ClientAccess.setShiftKeyProvider(Screen::hasShiftDown);

        // Enregistrement de la touche raccourci
        openBinderKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.cobblemon-cards.open_binder",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.cobblemon-cards"
        ));

        // Gestion du clic sur la touche
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openBinderKey.consumeClick()) {
                ClientPlayNetworking.send(new OpenBinderPayload());
            }
        });

        // ON ENREGISTRE LE PEINTRE DE CARTES
        // On s'assure d'enregistrer le CardItemRenderer de la bonne manière.
        // Attention au chemin d'import: on a vu com.howlite.cobblemoncards.render.CardItemRenderer et com.howlite.cobblemoncards.client.render.CardItemRenderer
        BuiltinItemRendererRegistry.INSTANCE.register(ModItems.CARD, new CardItemRenderer());
        
        // --- NOUVEAU POUR LE RENDER DE LA CARTE PAR DEFAUT ---
        // Il est possible que le renderer par défaut des cartes ne se déclenche plus
        // correctement à cause de changements dans la logique de rendu.

        // ENREGISTREMENT DE L'ECRAN DU CLASSEUR
        MenuScreens.register(ModMenuTypes.BINDER_MENU, BinderScreen::new);
        MenuScreens.register(ModMenuTypes.CARD_RECYCLER_MENU, CardRecyclerScreen::new);
        MenuScreens.register(ModMenuTypes.ADVANCED_HOLO_PROJECTOR_MENU, AdvancedHoloProjectorScreen::new);
        MenuScreens.register(ModMenuTypes.CARD_CABINET_MENU, CardCabinetScreen::new);

        // CONFIGURATION DU RENDU TRANSPARENT POUR LE BLOC
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.GRADING_STATION, RenderType.cutout());

        // ENREGISTREMENT DU RENDERER POUR LA STATION DE GRADAGE
        BlockEntityRenderers.register(ModBlockEntities.GRADING_STATION_BE, GradingStationBlockEntityRenderer::new);

        // ENREGISTREMENT DU RENDERER POUR LE PROJECTEUR HOLOGRAPHIQUE
        BlockEntityRenderers.register(ModBlockEntities.HOLO_PROJECTOR_BE, HoloProjectorBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.ADVANCED_HOLO_PROJECTOR_BE, AdvancedHoloProjectorBlockEntityRenderer::new);

        // ENREGISTREMENT DU RENDERER POUR LE MEUBLE A CARTES
        BlockEntityRenderers.register(ModBlockEntities.CARD_CABINET_BE, CardCabinetBlockEntityRenderer::new);

        // SMS 1 : Le serveur demande d'ouvrir l'écran
        ClientPlayNetworking.registerGlobalReceiver(OpenBoosterPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                Minecraft.getInstance().setScreen(new BoosterPackScreen());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(OpenWorkshopPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                Minecraft.getInstance().setScreen(new com.howlite.cobblemoncards.screen.CardWorkshopScreen());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SyncDiscoveredCardsPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                Minecraft.getInstance().setScreen(new com.howlite.cobblemoncards.screen.CardDexScreen(payload.discoveredIds()));
            });
        });

        // SMS 2 : Le serveur envoie l'item gagné !
        ClientPlayNetworking.registerGlobalReceiver(GiveRewardPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                // On vérifie que l'écran actuel est bien notre écran de Booster
                if (Minecraft.getInstance().screen instanceof BoosterPackScreen boosterScreen) {
                    // On donne l'item à l'écran pour qu'il puisse l'afficher !
                    boosterScreen.setRewards(payload.rewards());
                }
            });
        });
    }
}
