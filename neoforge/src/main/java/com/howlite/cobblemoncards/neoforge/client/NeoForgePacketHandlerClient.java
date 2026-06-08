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
}
