package com.howlite.cobblemoncards.util;

import java.util.function.Supplier;

public class ClientAccess {
    private static Supplier<Boolean> shiftKeyProvider = () -> false;

    public static void setShiftKeyProvider(Supplier<Boolean> provider) {
        shiftKeyProvider = provider;
    }

    public static boolean isShiftDown() {
        return shiftKeyProvider.get();
    }
}