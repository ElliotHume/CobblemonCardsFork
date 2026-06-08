package com.howlite.cobblemoncards.item.custom;

import com.howlite.cobblemoncards.block.entity.GradingStationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DebugItem extends Item {
    public DebugItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof GradingStationBlockEntity gradingStation) {
            if (!level.isClientSide) {
                if (gradingStation.getTimer() > 0) {
                    gradingStation.setTimer(1); // Set to 1 so the next tick finishes it
                    context.getPlayer().sendSystemMessage(Component.literal("Grading timer set to 1 tick."));
                } else {
                    context.getPlayer().sendSystemMessage(Component.literal("Grading station is not currently grading."));
                }
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}