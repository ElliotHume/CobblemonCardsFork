package com.howlite.cobblemoncards.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CardRecyclerMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;

    // Index des slots :
    // 0-11 : Slots d'entrée (machine)
    // 12 : Slot de sortie (machine)
    // 13-39 : Inventaire principal (joueur)
    // 40-48 : Barre d'accès rapide (joueur)

    public CardRecyclerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(13), new SimpleContainerData(2));
    }

    public CardRecyclerMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModMenuTypes.CARD_RECYCLER_MENU, containerId);
        checkContainerSize(container, 13);
        this.container = container;
        this.data = data;

        // Grille d'entrée 3x4
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 4; ++col) {
                this.addSlot(new Slot(container, col + row * 4, 8 + col * 18, 17 + row * 18));
            }
        }

        // Slot de sortie
        this.addSlot(new FurnaceResultSlot(playerInventory.player, container, 12, 116, 35));

        addDataSlots(data);

        // Player inventory
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            // Si on clique sur le slot de sortie
            if (index == 12) {
                if (!this.moveItemStackTo(itemstack1, 13, 49, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
            } 
            // Si on clique sur un slot d'entrée de la machine
            else if (index < 12) {
                if (!this.moveItemStackTo(itemstack1, 13, 49, false)) {
                    return ItemStack.EMPTY;
                }
            } 
            // Si on clique dans l'inventaire du joueur
            else {
                // On tente d'envoyer vers les slots d'entrée de la machine
                if (!this.moveItemStackTo(itemstack1, 0, 12, false)) {
                    // Si machine pleine, transfert entre hotbar et inventaire
                    if (index < 40) {
                        if (!this.moveItemStackTo(itemstack1, 40, 49, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(itemstack1, 13, 40, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    public int getScaledProgress() {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);
        return maxProgress != 0 && progress != 0 ? progress * 24 / maxProgress : 0;
    }
}
