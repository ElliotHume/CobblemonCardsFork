package com.howlite.cobblemoncards.block;

import com.howlite.cobblemoncards.block.entity.AdvancedHoloProjectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
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

public class AdvancedHoloProjectorBlock extends BaseEntityBlock {
    public static final MapCodec<AdvancedHoloProjectorBlock> CODEC = simpleCodec(AdvancedHoloProjectorBlock::new);
    
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 3, 16),
            Block.box(5, 3, 5, 11, 4, 11)
    );

    public AdvancedHoloProjectorBlock(Properties properties) {
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
        return new AdvancedHoloProjectorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.ADVANCED_HOLO_PROJECTOR_BE, AdvancedHoloProjectorBlockEntity::tick);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof AdvancedHoloProjectorBlockEntity advancedEntity) {
                // Arrêter la musique Jukebox de manière sécurisée
                AdvancedHoloProjectorBlockEntity.stopAllPokemonMusic(level, pos);
                Containers.dropContents(level, pos, advancedEntity);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AdvancedHoloProjectorBlockEntity advancedEntity) {

            if (player.isShiftKeyDown()) {
                if (!level.isClientSide) {
                    advancedEntity.cycleDisplayMode();
                    player.displayClientMessage(Component.translatable("message.cobblemon-cards.holo_projector.mode." + advancedEntity.getDisplayMode()), true);
                    level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.3f, 0.6f);
                }
                return InteractionResult.SUCCESS;
            }

            // Ouvre l'interface (GUI) du coffre au lieu de drop/insert un par un
            if (!level.isClientSide) {
                player.openMenu(advancedEntity);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AdvancedHoloProjectorBlockEntity advancedEntity) {

            if (player.isShiftKeyDown()) {
                if (!level.isClientSide) {
                    advancedEntity.cycleDisplayMode();
                    player.displayClientMessage(Component.translatable("message.cobblemon-cards.holo_projector.mode." + advancedEntity.getDisplayMode()), true);
                    level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.3f, 0.6f);
                }
                return ItemInteractionResult.SUCCESS;
            }

            // Ouvre l'interface (GUI)
            if (!level.isClientSide) {
                player.openMenu(advancedEntity);
            }
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
