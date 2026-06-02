package com.howlite.cobblemoncards.block;

import com.howlite.cobblemoncards.block.entity.CardCabinetBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

public class CardCabinetBlock extends BaseEntityBlock {
    public static final MapCodec<CardCabinetBlock> CODEC = simpleCodec(CardCabinetBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    /** 0 = empty, 1-7 = progressively fuller (maps to card_cabinet_front_1 … front_7 textures) */
    public static final IntegerProperty FILL_LEVEL = IntegerProperty.create("fill_level", 0, 7);

    public CardCabinetBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FILL_LEVEL, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            Block.box(1.0, 0.0, 1.0, 15.0, 13.0, 15.0),
            Block.box(1.0, 13.0, 5.0, 15.0, 15.0, 15.0),
            Block.box(1.0, 15.0, 10.0, 15.0, 17.0, 15.0)
    );

    private static final VoxelShape SHAPE_SOUTH = Shapes.or(
            Block.box(1.0, 0.0, 1.0, 15.0, 13.0, 15.0),
            Block.box(1.0, 13.0, 1.0, 15.0, 15.0, 11.0),
            Block.box(1.0, 15.0, 1.0, 15.0, 17.0, 6.0)
    );

    private static final VoxelShape SHAPE_EAST = Shapes.or(
            Block.box(1.0, 0.0, 1.0, 15.0, 13.0, 15.0),
            Block.box(1.0, 13.0, 1.0, 11.0, 15.0, 15.0),
            Block.box(1.0, 15.0, 1.0, 6.0, 17.0, 15.0)
    );

    private static final VoxelShape SHAPE_WEST = Shapes.or(
            Block.box(1.0, 0.0, 1.0, 15.0, 13.0, 15.0),
            Block.box(5.0, 13.0, 1.0, 15.0, 15.0, 15.0),
            Block.box(10.0, 15.0, 1.0, 15.0, 17.0, 15.0)
    );

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(FACING);
        return switch (direction) {
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FILL_LEVEL);
    }

    // --- Comparator support ---
    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CardCabinetBlockEntity cabinet) {
            return AbstractContainerMenu.getRedstoneSignalFromContainer(cabinet);
        }
        return 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CardCabinetBlockEntity(pos, state);
    }

    @Override
    protected java.util.List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        return java.util.Collections.emptyList();
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CardCabinetBlockEntity cabinetEntity) {
                ItemStack masterAlbum = new ItemStack(com.howlite.cobblemoncards.item.ModItems.MASTER_ALBUM);

                java.util.List<ItemStack> items = new java.util.ArrayList<>();
                boolean isEmpty = true;
                for (int i = 0; i < cabinetEntity.getContainerSize(); i++) {
                    ItemStack itemStack = cabinetEntity.getItem(i);
                    items.add(itemStack);
                    if (!itemStack.isEmpty()) {
                        isEmpty = false;
                    }
                }

                if (!isEmpty) {
                    masterAlbum.set(net.minecraft.core.component.DataComponents.CONTAINER,
                            net.minecraft.world.item.component.ItemContainerContents.fromItems(items));
                }

                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), masterAlbum);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CardCabinetBlockEntity cabinetEntity) {
            if (stack.has(net.minecraft.core.component.DataComponents.CONTAINER)) {
                net.minecraft.world.item.component.ItemContainerContents contents = 
                        stack.get(net.minecraft.core.component.DataComponents.CONTAINER);
                if (contents != null) {
                    net.minecraft.core.NonNullList<ItemStack> stacks = net.minecraft.core.NonNullList.withSize(12000, ItemStack.EMPTY);
                    contents.copyInto(stacks);
                    net.minecraft.core.NonNullList<ItemStack> inventoryList = cabinetEntity.getItems();
                    for (int i = 0; i < 12000; i++) {
                        ItemStack itemStack = stacks.get(i);
                        inventoryList.set(i, itemStack);
                        if (itemStack.getCount() > cabinetEntity.getMaxStackSize()) {
                            itemStack.setCount(cabinetEntity.getMaxStackSize());
                        }
                    }
                    cabinetEntity.setChanged();
                }
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CardCabinetBlockEntity cabinetEntity) {
                player.openMenu(cabinetEntity);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CardCabinetBlockEntity cabinetEntity) {
                player.openMenu(cabinetEntity);
            }
        }
        return ItemInteractionResult.SUCCESS;
    }
}
