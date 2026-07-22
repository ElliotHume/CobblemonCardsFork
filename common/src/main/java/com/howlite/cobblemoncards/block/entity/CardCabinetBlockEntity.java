package com.howlite.cobblemoncards.block.entity;

import com.howlite.cobblemoncards.block.CardCabinetBlock;
import com.howlite.cobblemoncards.menu.CardCabinetMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import com.howlite.cobblemoncards.util.CardAdvancementManager;
import net.minecraft.server.level.ServerPlayer;

public class CardCabinetBlockEntity extends BlockEntity implements ImplementedInventory, MenuProvider {
    // 1000 pages of 12 slots each = 12000 slots total (practically infinite card storage)
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(12000, ItemStack.EMPTY);

    public CardCabinetBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CARD_CABINET_BE, pos, state);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveCabinetItems(tag, inventory, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.clear();
        loadCabinetItems(tag, inventory, registries);
    }

    public static CompoundTag saveCabinetItems(CompoundTag tag, NonNullList<ItemStack> items, HolderLookup.Provider registries) {
        ListTag listTag = new ListTag();
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                stack.save(registries, itemTag);
                listTag.add(itemTag);
            }
        }
        tag.put("Items", listTag);
        return tag;
    }

    public static void loadCabinetItems(CompoundTag tag, NonNullList<ItemStack> items, HolderLookup.Provider registries) {
        items.clear();
        ListTag listTag = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag itemTag = listTag.getCompound(i);
            int slot;
            if (itemTag.contains("Slot", Tag.TAG_INT)) {
                slot = itemTag.getInt("Slot");
            } else {
                slot = itemTag.getByte("Slot") & 0xFF;
            }
            if (slot >= 0 && slot < items.size()) {
                items.set(slot, ItemStack.parse(registries, itemTag).orElse(ItemStack.EMPTY));
            }
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) {
            // Recompute fill level (0-7) and update block state texture
            int newFillLevel = computeFillLevel();
            BlockState currentState = getBlockState();
            if (currentState.getValue(CardCabinetBlock.FILL_LEVEL) != newFillLevel) {
                level.setBlock(getBlockPos(),
                        currentState.setValue(CardCabinetBlock.FILL_LEVEL, newFillLevel),
                        3); // flag 3 = update + notify neighbours (comparator)
            } else {
                level.sendBlockUpdated(getBlockPos(), currentState, currentState, 3);
            }
            level.updateNeighbourForOutputSignal(getBlockPos(), getBlockState().getBlock());

            // Check if cabinet is completely full (12,000 cards)
            int count = 0;
            for (ItemStack stack : inventory) {
                if (!stack.isEmpty()) count++;
            }
            if (count == inventory.size()) {
                for (Player player : level.players()) {
                    if (player instanceof ServerPlayer serverPlayer && serverPlayer.containerMenu instanceof CardCabinetMenu cabinetMenu) {
                        if (cabinetMenu.getContainer() == this) {
                            CardAdvancementManager.grantAdvancement(serverPlayer, "full_cabinet");
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the fill level 0-7 based on how many card slots are occupied.
     * 0 = empty, 1 = a few cards, 7 = nearly or completely full.
     */
    private int computeFillLevel() {
        int count = 0;
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) count++;
        }
        if (count == 0) return 0;
        // Map 1..12000 → 1..7 (ceiling division into 7 equal buckets)
        return Math.min(7, 1 + (count - 1) * 7 / inventory.size());
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.cobblemon-cards.card_cabinet");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            int count = 0;
            for (ItemStack stack : inventory) {
                if (!stack.isEmpty()) count++;
            }
            if (count == inventory.size()) {
                CardAdvancementManager.grantAdvancement(serverPlayer, "full_cabinet");
            }
        }
        return new CardCabinetMenu(syncId, playerInventory, this);
    }
}
