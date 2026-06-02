package com.howlite.cobblemoncards.client;

import net.minecraft.client.gui.screens.Screen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ClientTooltipHelper {
    public static boolean isShiftKeyDown() {
        return Screen.hasShiftDown();
    }
}
