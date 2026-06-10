package com.howlite.cobblemoncards.block.entity;

import com.howlite.cobblemoncards.CobblemonCardsConfig;
import com.howlite.cobblemoncards.component.CardData;
import com.howlite.cobblemoncards.component.ModDataComponents;
import com.howlite.cobblemoncards.item.ModItems;
import com.howlite.cobblemoncards.menu.CardRecyclerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

public class CardRecyclerBlockEntity extends BlockEntity implements ImplementedInventory, MenuProvider, WorldlyContainer {
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(13, ItemStack.EMPTY);
    public int progress = 0;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> CardRecyclerBlockEntity.this.progress;
                case 1 -> CobblemonCardsConfig.recyclerProcessTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) CardRecyclerBlockEntity.this.progress = value;
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public CardRecyclerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CARD_RECYCLER_BE, pos, state);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, this.inventory, registries);
        this.progress = tag.getInt("progress");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.inventory, registries);
        tag.putInt("progress", this.progress);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        int inputSlot = findFirstValidInputSlot();
        if (inputSlot != -1) {
            ItemStack input = getItem(inputSlot);
            if (canOutput(input)) {
                progress++;
                if (progress >= CobblemonCardsConfig.recyclerProcessTime) {
                    craftItem(inputSlot);
                    progress = 0;
                }
                setChanged();
                return;
            }
        }

        if (progress != 0) {
            progress = 0;
            setChanged();
        }
    }

    private int findFirstValidInputSlot() {
        for (int i = 0; i < 12; i++) {
            ItemStack stack = getItem(i);
            if (!stack.isEmpty() && stack.has(ModDataComponents.CARD_DATA)) {
                return i;
            }
        }
        return -1;
    }

    private boolean canOutput(ItemStack input) {
        CardData data = input.get(ModDataComponents.CARD_DATA);
        if (data == null) return false;

        int dustAmount = calculateDustAmount(data);
        if (dustAmount <= 0) return false;

        ItemStack outputSlot = getItem(12);
        if (outputSlot.isEmpty()) return true;
        if (!outputSlot.is(ModItems.CARD_DUST)) return false;
        
        return outputSlot.getCount() + dustAmount <= outputSlot.getMaxStackSize();
    }

    private void craftItem(int slotIndex) {
        ItemStack input = getItem(slotIndex);
        CardData data = input.get(ModDataComponents.CARD_DATA);
        if (data == null) return;

        int dustAmount = calculateDustAmount(data);
        if (dustAmount <= 0) return;
        input.shrink(1);
        
        ItemStack outputStack = getItem(12);
        if (outputStack.isEmpty()) {
            setItem(12, new ItemStack(ModItems.CARD_DUST, dustAmount));
        } else {
            outputStack.setCount(outputStack.getCount() + dustAmount);
        }
        
        setChanged();
    }

    private int calculateDustAmount(CardData data) {
        // Les cartes cosmétiques / de joueur ne donnent pas de card dust
        if (com.howlite.cobblemoncards.util.CardUtil.isCosmeticCard(data.pokemonId())) {
            return 0;
        }

        int amount = switch (data.rarity().toLowerCase()) {
            case "common" -> 1;
            case "uncommon" -> 2;
            case "rare" -> 3;
            case "epic" -> 5;
            case "legendary" -> 10;
            case "mythic" -> 15;
            default -> 1;
        };

        if (data.isShiny()) amount *= 2;
        if (shouldHaveBackground(data)) amount += 2;
        if (hasHoloEffect(data)) amount += 3;

        return amount;
    }

    private boolean hasHoloEffect(CardData data) {
        String rarity = data.rarity().toLowerCase();
        return !rarity.equals("common") || data.isShiny();
    }

    private boolean shouldHaveBackground(CardData data) {
        String rarity = data.rarity().toLowerCase();
        if (rarity.equals("rare") || rarity.equals("epic") || rarity.equals("legendary") || data.isShiny()) {
            return Math.abs(data.pokemonId().hashCode() + data.rarity().hashCode()) % 2 == 0;
        }
        return false;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.cobblemon-cards.card_recycler");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CardRecyclerMenu(containerId, playerInventory, this, this.dataAccess);
    }

    // WorldlyContainer (Hopper Support)
    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) {
            return new int[]{12};
        }
        return IntStream.range(0, 12).toArray();
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
        return index < 12 && itemStack.has(ModDataComponents.CARD_DATA);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == 12;
    }
}
