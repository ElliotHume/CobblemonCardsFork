package com.howlite.cobblemoncards.block;

import com.howlite.cobblemoncards.block.entity.HoloProjectorBlockEntity;
import com.howlite.cobblemoncards.item.custom.CardItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.mojang.serialization.MapCodec;
import com.howlite.cobblemoncards.block.entity.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

public class HoloProjectorBlock extends BaseEntityBlock {
    public static final MapCodec<HoloProjectorBlock> CODEC = simpleCodec(HoloProjectorBlock::new);
    
    // Shape correspondant au modèle: Base [0,0,0] -> [16,3,16] et Milieu [5,3,5] -> [11,4,11]
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 3, 16),
            Block.box(5, 3, 5, 11, 4, 11)
    );

    public HoloProjectorBlock(Properties properties) {
        super(properties);
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HoloProjectorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.HOLO_PROJECTOR_BE, HoloProjectorBlockEntity::tick);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof HoloProjectorBlockEntity holoEntity) {
                // Arrêter la musique Jukebox de manière sécurisée
                HoloProjectorBlockEntity.stopAllPokemonMusic(level, pos);
                if (!holoEntity.getItem(0).isEmpty()) {
                    Block.popResource(level, pos, holoEntity.getItem(0));
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof HoloProjectorBlockEntity holoEntity) {
            ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack itemInBlock = holoEntity.getItem(0);

            if (player.isShiftKeyDown()) {
                // Cycle mode d'affichage si on s'accroupit
                if (!level.isClientSide) {
                    holoEntity.cycleDisplayMode();
                    player.displayClientMessage(Component.translatable("message.cobblemon-cards.holo_projector.mode." + holoEntity.getDisplayMode()), true);
                    // Petit clic mécanique
                    level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.3f, 0.6f);
                }
                return InteractionResult.SUCCESS;
            }

            if (itemInHand.isEmpty() && !itemInBlock.isEmpty()) {
                // Remove item from block
                player.setItemInHand(InteractionHand.MAIN_HAND, itemInBlock.copy());
                holoEntity.setItem(0, ItemStack.EMPTY);
                holoEntity.setChanged();
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
                
                if (!level.isClientSide) {
                    // Son quand on récupère
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.2f);
                }
                return InteractionResult.SUCCESS;
            } else if (!itemInHand.isEmpty() && itemInBlock.isEmpty()) {
                // Insert item into block
                if (itemInHand.getItem() instanceof CardItem) {
                    holoEntity.setItem(0, itemInHand.copyWithCount(1));
                    if (!player.isCreative()) {
                        itemInHand.shrink(1);
                    }
                    holoEntity.setChanged();
                    level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
                    
                    if (!level.isClientSide) {
                        // Son quand on pose
                        level.playSound(null, pos, SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 0.8f, 1.0f);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof HoloProjectorBlockEntity holoEntity) {
            ItemStack itemInBlock = holoEntity.getItem(0);

            if (player.isShiftKeyDown()) {
                if (!level.isClientSide) {
                    holoEntity.cycleDisplayMode();
                    player.displayClientMessage(Component.translatable("message.cobblemon-cards.holo_projector.mode." + holoEntity.getDisplayMode()), true);
                    level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.3f, 0.6f);
                }
                return ItemInteractionResult.SUCCESS;
            }

            if (!stack.isEmpty() && itemInBlock.isEmpty()) {
                // Insert item into block
                if (stack.getItem() instanceof CardItem) {
                    holoEntity.setItem(0, stack.copyWithCount(1));
                    if (!player.isCreative()) {
                        stack.shrink(1);
                    }
                    holoEntity.setChanged();
                    level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
                    
                    if (!level.isClientSide) {
                        level.playSound(null, pos, SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 0.8f, 1.0f);
                    }
                    return ItemInteractionResult.SUCCESS;
                }
            } else if (stack.isEmpty() && !itemInBlock.isEmpty()) {
                player.setItemInHand(hand, itemInBlock.copy());
                holoEntity.setItem(0, ItemStack.EMPTY);
                holoEntity.setChanged();
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
                
                if (!level.isClientSide) {
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.2f);
                }
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
