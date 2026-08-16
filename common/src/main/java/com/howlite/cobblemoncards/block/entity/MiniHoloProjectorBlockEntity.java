package com.howlite.cobblemoncards.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MiniHoloProjectorBlockEntity extends BlockEntity implements ImplementedInventory {
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
    private final int[] displayModes = new int[]{0, 0, 0, 0};

    public MiniHoloProjectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MINI_HOLO_PROJECTOR_BE, pos, state);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return inventory;
    }

    public int getDisplayMode(int slot) {
        if (slot >= 0 && slot < 4) {
            return displayModes[slot];
        }
        return 0;
    }

    public void cycleDisplayMode(int slot) {
        if (slot >= 0 && slot < 4) {
            this.displayModes[slot] = (this.displayModes[slot] + 1) % 6;
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, inventory, registries);
        tag.putIntArray("DisplayModes", displayModes);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.clear();
        ContainerHelper.loadAllItems(tag, inventory, registries);
        if (tag.contains("DisplayModes")) {
            int[] modes = tag.getIntArray("DisplayModes");
            for (int i = 0; i < Math.min(modes.length, 4); i++) {
                displayModes[i] = modes[i];
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
        return saveWithoutMetadata(registries);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
