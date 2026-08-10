package com.howlite.cobblemoncards.neoforge.client;

import com.howlite.cobblemoncards.network.*;
import com.howlite.cobblemoncards.screen.BoosterPackScreen;
import net.minecraft.client.Minecraft;

public class NeoForgePacketHandlerClient {

    public static void handleOpenBooster(OpenBoosterPayload payload) {
        Minecraft.getInstance().setScreen(new BoosterPackScreen());
    }

    public static void handleGiveReward(GiveRewardPayload payload) {
        if (Minecraft.getInstance().screen instanceof BoosterPackScreen boosterScreen) {
            boosterScreen.setRewards(payload.rewards());
        }
    }

    public static void handleOpenWorkshop(OpenWorkshopPayload payload) {
        Minecraft.getInstance().setScreen(new com.howlite.cobblemoncards.screen.CardWorkshopScreen());
    }

    public static void handleSyncDiscoveredCards(SyncDiscoveredCardsPayload payload) {
        Minecraft.getInstance().setScreen(new com.howlite.cobblemoncards.screen.CardDexScreen(payload.discoveredIds()));
    }

    public static void handleRenderCard(RenderCardPayload payload) {
        Minecraft.getInstance().setScreen(new com.howlite.cobblemoncards.screen.CardShowcaseScreen(payload.cards()));
    }

    public static void handleInspectCard(com.howlite.cobblemoncards.network.InspectCardPayload payload) {
        Minecraft.getInstance().setScreen(new com.howlite.cobblemoncards.screen.CardInspectScreen(payload.card()));
    }

    public static void handleShowCard(com.howlite.cobblemoncards.network.ShowCardPayload payload) {
        com.howlite.cobblemoncards.render.CardShowRenderer.onPlayerShow(payload.holderUuid(), payload.card());
    }

    public static void handleStopShowCard(com.howlite.cobblemoncards.network.StopShowCardPayload payload) {
        com.howlite.cobblemoncards.render.CardShowRenderer.stopPlayerShow(payload.holderUuid());
    }
}
