package com.howlite.cobblemoncards.neoforge.client;

import com.howlite.cobblemoncards.client.CobblemonCardsClient;
import com.howlite.cobblemoncards.component.ModDataComponents;
import com.howlite.cobblemoncards.network.OpenBinderPayload;
import com.howlite.cobblemoncards.render.ModShaders;
import com.howlite.cobblemoncards.screen.AdvancedHoloProjectorScreen;
import com.howlite.cobblemoncards.screen.BinderScreen;
import com.howlite.cobblemoncards.screen.CardCabinetScreen;
import com.howlite.cobblemoncards.screen.CardRecyclerScreen;
import com.howlite.cobblemoncards.menu.ModMenuTypes;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;

@EventBusSubscriber(modid = "cobblemon_cards", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NeoForgeCobblemonCardsClient {
    private static KeyMapping openBinderKey;
    private static KeyMapping openShowcaseKey;

    public static void registerClient(IEventBus modEventBus) {
        // Enregistrer les événements sur le bus du jeu pour les ticks client
        NeoForge.EVENT_BUS.addListener(NeoForgeCobblemonCardsClient::onClientTick);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            CobblemonCardsClient.init();
        });
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.BINDER_MENU, BinderScreen::new);
        event.register(ModMenuTypes.CARD_RECYCLER_MENU, CardRecyclerScreen::new);
        event.register(ModMenuTypes.ADVANCED_HOLO_PROJECTOR_MENU, AdvancedHoloProjectorScreen::new);
        event.register(ModMenuTypes.CARD_CABINET_MENU, CardCabinetScreen::new);
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new net.minecraft.client.renderer.ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "card_procedural_bg"),
                        DefaultVertexFormat.NEW_ENTITY
                ),
                shader -> ModShaders.bgShader = shader
        );
        event.registerShader(
                new net.minecraft.client.renderer.ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "card_procedural_holo"),
                        DefaultVertexFormat.NEW_ENTITY
                ),
                shader -> ModShaders.holoShader = shader
        );
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        openBinderKey = new KeyMapping(
                "key.cobblemon-cards.open_binder",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.cobblemon-cards"
        );
        openShowcaseKey = new KeyMapping(
                "key.cobblemon-cards.open_showcase",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.cobblemon-cards"
        );
        event.register(openBinderKey);
        event.register(openShowcaseKey);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        if (openBinderKey == null || openShowcaseKey == null) return;
        
        while (openBinderKey.consumeClick()) {
            com.howlite.cobblemoncards.util.PlatformHelper.INSTANCE.sendToServer(new OpenBinderPayload());
        }

        while (openShowcaseKey.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.level != null) {
                java.util.List<net.minecraft.world.item.ItemStack> showcaseCards = new java.util.ArrayList<>();
                for (net.minecraft.world.item.ItemStack stack : mc.player.getInventory().items) {
                    if (stack.has(ModDataComponents.CARD_DATA)) {
                        showcaseCards.add(stack.copy());
                        if (showcaseCards.size() >= 10) break;
                    }
                }
                if (!showcaseCards.isEmpty()) {
                    mc.setScreen(new com.howlite.cobblemoncards.screen.CardShowcaseScreen(showcaseCards));
                }
            }
        }
    }
}
