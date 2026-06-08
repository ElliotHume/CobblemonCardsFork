package com.howlite.cobblemoncards.block;

import com.howlite.cobblemoncards.CobblemonCardsConfig;
import com.howlite.cobblemoncards.block.entity.GradingStationBlockEntity;
import com.howlite.cobblemoncards.block.entity.ModBlockEntities;
import com.howlite.cobblemoncards.component.CardData;
import com.howlite.cobblemoncards.component.ModDataComponents;
import com.howlite.cobblemoncards.item.custom.CardItem;
import com.howlite.cobblemoncards.item.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
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
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class GradingStationBlock extends BaseEntityBlock {
    public static final MapCodec<GradingStationBlock> CODEC = simpleCodec(GradingStationBlock::new);

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 3, 16),
            Block.box(0.5, 3, 0.5, 15.5, 18, 15.5)
    );

    public GradingStationBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GradingStationBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.GRADING_STATION_BE, GradingStationBlockEntity::tick);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof GradingStationBlockEntity) {
                // Réinitialisation du Moniteur à la destruction
                BlockPos monitorPos = pos.above();
                BlockState monitorState = level.getBlockState(monitorPos);
                ResourceLocation id = BuiltInRegistries.BLOCK.getKey(monitorState.getBlock());
                if (id.getNamespace().equals("cobblemon") && id.getPath().equals("monitor")) {
                    for (Property<?> prop : monitorState.getProperties()) {
                        if (prop.getName().equals("screen")) {
                            applyScreenValue(level, monitorPos, monitorState, prop, "off");
                            break;
                        }
                    }
                }

                Containers.dropContents(level, pos, (GradingStationBlockEntity)blockEntity);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    private <T extends Comparable<T>> void applyScreenValue(Level level, BlockPos pos, BlockState state, Property<T> property, String valueName) {
        Optional<T> value = property.getValue(valueName);
        if (value.isPresent()) {
            level.setBlock(pos, state.setValue(property, value.get()), Block.UPDATE_ALL);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof GradingStationBlockEntity blockEntity)) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;

        ItemStack itemInMachine = blockEntity.getItem(0);

        // Insertion
        if (itemInMachine.isEmpty()) {
            if (stack.getItem() instanceof CardItem) {
                CardData cardData = stack.get(ModDataComponents.CARD_DATA);
                if (cardData != null && cardData.grade() > 0) {
                    if (!level.isClientSide) {
                        player.displayClientMessage(Component.translatable("message.cobblemon-cards.grading_station.already_graded").withStyle(ChatFormatting.RED), true);
                    }
                    return ItemInteractionResult.FAIL;
                }

                if (isMonitorAbove(level, pos)) {
                    int dustCost = CobblemonCardsConfig.gradingStationDustCost;
                    if (!player.getAbilities().instabuild && dustCost > 0) {
                        int totalDust = 0;
                        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                            ItemStack invStack = player.getInventory().getItem(i);
                            if (invStack.is(ModItems.CARD_DUST)) {
                                totalDust += invStack.getCount();
                            }
                        }

                        if (totalDust < dustCost) {
                            if (!level.isClientSide) {
                                player.displayClientMessage(Component.translatable("message.cobblemon-cards.grading_station.dust_required", dustCost).withStyle(ChatFormatting.RED), true);
                            }
                            return ItemInteractionResult.FAIL;
                        }

                        if (!level.isClientSide) {
                            // Consume dust
                            int remainingToConsume = dustCost;
                            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                                ItemStack invStack = player.getInventory().getItem(i);
                                if (invStack.is(ModItems.CARD_DUST)) {
                                    int count = invStack.getCount();
                                    if (count >= remainingToConsume) {
                                        invStack.shrink(remainingToConsume);
                                        break;
                                    } else {
                                        remainingToConsume -= count;
                                        invStack.setCount(0);
                                    }
                                }
                            }
                        }
                    }

                    if (!level.isClientSide) {
                        blockEntity.setItem(0, stack.split(1));
                        blockEntity.setTimer(CobblemonCardsConfig.gradingStationProcessTime);
                        blockEntity.sync();

                        // SONS D'INSERTION
                        level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0f, 1.0f);
                        level.playSound(null, pos, SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("cobblemon", "block.fossil_machine.assemble")), SoundSource.BLOCKS, 1.0f, 1.0f);
                    }
                    return ItemInteractionResult.SUCCESS;
                } else {
                    if (!level.isClientSide) {
                        player.displayClientMessage(Component.translatable("message.cobblemon-cards.grading_station.monitor_required").withStyle(ChatFormatting.RED), true);
                    }
                    return ItemInteractionResult.FAIL;
                }
            }
        } 
        // Récupération
        else {
            if (blockEntity.getTimer() <= 0) {
                if (!level.isClientSide) {
                    player.getInventory().add(blockEntity.removeItem(0, 1));
                    blockEntity.sync();

                    if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                        com.howlite.cobblemoncards.util.CardAdvancementManager.checkAdvancements(serverPlayer);
                    }

                    // SONS DE RECUPERATION
                    level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0f, 1.0f);
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0f, 1.0f);
                }
                return ItemInteractionResult.SUCCESS;
            } else {
                if (!level.isClientSide) {
                    int remainingMinutes = (blockEntity.getTimer() / 20) / 60;
                    int remainingSeconds = (blockEntity.getTimer() / 20) % 60;
                    String timeStr = String.format("%02d:%02d", remainingMinutes, remainingSeconds);
                    player.displayClientMessage(Component.translatable("message.cobblemon-cards.grading_station.analysis_in_progress", timeStr).withStyle(ChatFormatting.YELLOW), true);
                }
                return ItemInteractionResult.SUCCESS;
            }
        }

        return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
    }

    private boolean isMonitorAbove(Level level, BlockPos pos) {
        BlockState aboveState = level.getBlockState(pos.above());
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(aboveState.getBlock());
        return id.getNamespace().equals("cobblemon") && id.getPath().equals("monitor");
    }
}