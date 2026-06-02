package com.howlite.cobblemoncards.item.custom;

public enum BinderTier {
    LEATHER(1),
    IRON(2),
    GOLD(3),
    DIAMOND(6),
    NETHERITE(10),
    MASTER(1000); // Ajustez le nombre de pages pour le Master Album

    private final int pages;

    BinderTier(int pages) {
        this.pages = pages;
    }

    public int getPages() {
        return pages;
    }

    public int getMaxSlots(int slotsPerPage) {
        return pages * slotsPerPage;
    }
}