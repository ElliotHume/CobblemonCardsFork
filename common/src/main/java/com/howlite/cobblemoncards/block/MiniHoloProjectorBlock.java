package com.howlite.cobblemoncards.block;

import com.howlite.cobblemoncards.block.entity.ModBlockEntities;
import com.howlite.cobblemoncards.block.entity.MiniHoloProjectorBlockEntity;
import com.howlite.cobblemoncards.item.custom.CardItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class MiniHoloProjectorBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<MiniHoloProjectorBlock> CODEC = simpleCodec(MiniHoloProjectorBlock::new);
    public static final IntegerProperty PROJECTORS = IntegerProperty.create("projectors", 1, 4);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 2, 16);

    public MiniHoloProjectorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PROJECTORS, 1).setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        if (!context.isSecondaryUseActive() && context.getItemInHand().getItem() == this.asItem() && state.getValue(PROJECTORS) < 4) {
            return true;
        }
        return super.canBeReplaced(state, context);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState currentState = context.getLevel().getBlockState(context.getClickedPos());
        if (currentState.is(this)) {
            return currentState.setValue(PROJECTORS, Math.min(4, currentState.getValue(PROJECTORS) + 1));
        }
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PROJECTORS, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MiniHoloProjectorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.MINI_HOLO_PROJECTOR_BE, (l, p, s, be) -> {});
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof MiniHoloProjectorBlockEntity miniEntity) {
                // Drop all inserted cards
                for (int i = 0; i < 4; i++) {
                    ItemStack stack = miniEntity.getItem(i);
                    if (!stack.isEmpty()) {
                        Block.popResource(level, pos, stack);
                    }
                }
                // Drop extra mini projector items if count > 1
                int extraProjectors = state.getValue(PROJECTORS) - 1;
                if (extraProjectors > 0) {
                    Block.popResource(level, pos, new ItemStack(this.asItem(), extraProjectors));
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public static int getHitSubSlot(BlockHitResult hit, BlockState state) {
        double relX = hit.getLocation().x - hit.getBlockPos().getX();
        double relZ = hit.getLocation().z - hit.getBlockPos().getZ();
        int count = state.getValue(PROJECTORS);

        int slot;
        if (relX < 0.5 && relZ < 0.5)      slot = 0; // NW
        else if (relX >= 0.5 && relZ < 0.5) slot = 1; // NE
        else if (relX < 0.5 && relZ >= 0.5) slot = 2; // SW
        else                                slot = 3; // SE

        return Math.min(slot, count - 1);
    }

    private InteractionResult handleInteraction(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult, ItemStack itemInHand) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof MiniHoloProjectorBlockEntity miniEntity)) {
            return InteractionResult.PASS;
        }

        // If player is holding a MiniHoloProjector and block can accept more, let placement handle it
        if (itemInHand.getItem() == this.asItem() && state.getValue(PROJECTORS) < 4 && !player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }

        int slot = getHitSubSlot(hitResult, state);
        ItemStack itemInSlot = miniEntity.getItem(slot);

        if (player.isShiftKeyDown()) {
            // Shift + Right Click: Cycle display mode for this sub-projector
            if (!level.isClientSide()) {
                miniEntity.cycleDisplayMode(slot);
                player.displayClientMessage(
                    Component.translatable("message.cobblemon-cards.mini_holo_projector.mode", (slot + 1), miniEntity.getDisplayMode(slot)),
                    true
                );
                level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.3f, 0.8f);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        // Inserting a card
        if (itemInHand.getItem() instanceof CardItem) {
            if (!level.isClientSide()) {
                ItemStack cardToPlace = itemInHand.copyWithCount(1);
                if (!itemInSlot.isEmpty()) {
                    // Swap card
                    player.setItemInHand(hand, itemInSlot.copy());
                } else {
                    if (!player.isCreative()) {
                        itemInHand.shrink(1);
                    }
                }
                miniEntity.setItem(slot, cardToPlace);
                miniEntity.setChanged();
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
                level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 0.8f, 1.2f);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        // Removing a card with empty hand / non-card item
        if (!itemInSlot.isEmpty()) {
            if (!level.isClientSide()) {
                if (itemInHand.isEmpty()) {
                    player.setItemInHand(hand, itemInSlot.copy());
                } else {
                    Block.popResource(level, pos, itemInSlot.copy());
                }
                miniEntity.setItem(slot, ItemStack.EMPTY);
                miniEntity.setChanged();
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.0f);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return handleInteraction(state, level, pos, player, InteractionHand.MAIN_HAND, hitResult, player.getItemInHand(InteractionHand.MAIN_HAND));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        InteractionResult res = handleInteraction(state, level, pos, player, hand, hitResult, stack);
        return res == InteractionResult.PASS ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION : ItemInteractionResult.sidedSuccess(level.isClientSide());
    }
}
