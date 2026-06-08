package com.howlite.cobblemoncards.fabric.client;

import com.howlite.cobblemoncards.client.CobblemonCardsClient;
import com.howlite.cobblemoncards.CobblemonCardsConfig;
import com.howlite.cobblemoncards.block.ModBlocks;
import com.howlite.cobblemoncards.component.ModDataComponents;
import com.howlite.cobblemoncards.item.ModItems;
import com.howlite.cobblemoncards.network.*;
import com.howlite.cobblemoncards.render.CardItemRenderer;
import com.howlite.cobblemoncards.render.ModShaders;
import com.howlite.cobblemoncards.screen.BoosterPackScreen;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class FabricCobblemonCardsClient implements ClientModInitializer {
    private static KeyMapping openBinderKey;
    private static KeyMapping openShowcaseKey;

    @Override
    public void onInitializeClient() {
        // 1. Initialiser le client commun
        CobblemonCardsClient.init();

        // 2. Enregistrer les shaders custom via l'API Fabric
        CoreShaderRegistrationCallback.EVENT.register(context -> {
            context.register(
                    ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "card_procedural_bg"),
                    DefaultVertexFormat.NEW_ENTITY,
                    shader -> ModShaders.bgShader = shader
            );
            context.register(
                    ResourceLocation.fromNamespaceAndPath("cobblemon-cards", "card_procedural_holo"),
                    DefaultVertexFormat.NEW_ENTITY,
                    shader -> ModShaders.holoShader = shader
            );
        });

        // 3. Initialisation de la configuration MidnightLib Client
        MidnightConfig.init("cobblemon-cards", CobblemonCardsConfig.class);

        // 4. Enregistrement des touches raccourcis
        openBinderKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.cobblemon-cards.open_binder",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.cobblemon-cards"
        ));

        openShowcaseKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.cobblemon-cards.open_showcase",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.cobblemon-cards"
        ));

        // 5. Gestion du tick client pour les touches
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openBinderKey.consumeClick()) {
                ClientPlayNetworking.send(new OpenBinderPayload());
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
        });

        // 6. Enregistrement du peintre de cartes
        BuiltinItemRendererRegistry.INSTANCE.register(ModItems.CARD, CardItemRenderer::render);

        // 7. Rendu transparent du bloc Grading Station
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.GRADING_STATION, RenderType.cutout());

        // 8. Enregistrement des récepteurs de paquets client
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

        ClientPlayNetworking.registerGlobalReceiver(RenderCardPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                Minecraft.getInstance().setScreen(
                        new com.howlite.cobblemoncards.screen.CardShowcaseScreen(payload.cards()));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(GiveRewardPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (Minecraft.getInstance().screen instanceof BoosterPackScreen boosterScreen) {
                    boosterScreen.setRewards(payload.rewards());
                }
            });
        });
    }
}
