package com.howlite.cobblemoncards.menu;

import com.howlite.cobblemoncards.component.CardData;
import com.howlite.cobblemoncards.component.ModDataComponents;
import com.howlite.cobblemoncards.item.custom.CardItem;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CardCabinetMenu extends AbstractContainerMenu {
    public static final int SLOTS_PER_PAGE = 12; // 4x3
    public static final int MAX_PAGES = 1000;

    private final Container cabinetContainer;
    private int currentPage = 0;

    public CardCabinetMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(12000));
    }

    public CardCabinetMenu(int containerId, Inventory playerInventory, Container cabinetContainer) {
        super(ModMenuTypes.CARD_CABINET_MENU, containerId);
        this.cabinetContainer = cabinetContainer;

        for (int p = 0; p < MAX_PAGES; p++) {
            for (int row = 0; row < 3; ++row) {
                for (int col = 0; col < 4; ++col) {
                    int index = col + row * 4 + p * SLOTS_PER_PAGE;
                    int xPos = 46 + col * 50;
                    int yPos = 35 + row * 65;
                    this.addSlot(new CardSlot(this.cabinetContainer, index, xPos, yPos, p, this));
                }
            }
        }

        // Inventaire du joueur
        int invY = 229;
        int invX = 49;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, invX + col * 18, invY + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, invX + col * 18, invY + 58));
        }
    }

    public Container getContainer() {
        return this.cabinetContainer;
    }

    public void setPage(int page) {
        if (page >= 0 && page < MAX_PAGES) {
            this.currentPage = page;
        }
    }

    private int getRarityWeight(String rarity) {
        if (rarity == null) return 0;
        return switch (rarity.toLowerCase()) {
            case "common" -> 1;
            case "uncommon" -> 2;
            case "rare" -> 3;
            case "epic" -> 4;
            case "legendary" -> 5;
            case "mythic" -> 6;
            default -> 0;
        };
    }

    public void sort(int sortMode) {
        List<ItemStack> cards = new ArrayList<>();
        int totalSlots = MAX_PAGES * SLOTS_PER_PAGE;
        for (int i = 0; i < totalSlots; i++) {
            ItemStack stack = this.cabinetContainer.getItem(i);
            if (!stack.isEmpty()) {
                cards.add(stack);
                this.cabinetContainer.setItem(i, ItemStack.EMPTY);
            }
        }

        Comparator<ItemStack> comparator = (s1, s2) -> {
            CardData d1 = s1.get(ModDataComponents.CARD_DATA);
            CardData d2 = s2.get(ModDataComponents.CARD_DATA);
            if (d1 == null && d2 == null) return 0;
            if (d1 == null) return 1;
            if (d2 == null) return -1;

            return switch (sortMode) {
                case 1 -> d1.pokemonId().compareTo(d2.pokemonId());
                case 2 -> Integer.compare(getRarityWeight(d2.rarity()), getRarityWeight(d1.rarity()));
                case 3 -> Integer.compare(d2.grade(), d1.grade());
                default -> 0;
            };
        };

        cards.sort(comparator);

        for (int i = 0; i < cards.size() && i < totalSlots; i++) {
            this.cabinetContainer.setItem(i, cards.get(i));
        }
    }

    public int getCurrentPage() { return currentPage; }
    public int getMaxPages() { return MAX_PAGES; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        int totalCabinetSlots = MAX_PAGES * SLOTS_PER_PAGE;
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();

            if (index < totalCabinetSlots) {
                if (!this.moveItemStackTo(originalStack, totalCabinetSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (originalStack.getItem() instanceof CardItem) {
                    if (!this.moveItemStackTo(originalStack, 0, totalCabinetSlots, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return newStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.cabinetContainer.stillValid(player);
    }

    private static class CardSlot extends Slot {
        private final int page;
        private final CardCabinetMenu menu;

        public CardSlot(Container container, int index, int x, int y, int page, CardCabinetMenu menu) {
            super(container, index, x, y);
            this.page = page;
            this.menu = menu;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof CardItem;
        }

        @Override
        public boolean isActive() {
            return this.menu.getCurrentPage() == this.page;
        }
    }
}
