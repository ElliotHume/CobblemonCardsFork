package com.howlite.cobblemoncards.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record BinderLocator(Type type, String slotName, int index) {
    public enum Type {
        HAND,
        ACCESSORY,
        INVENTORY
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, BinderLocator> STREAM_CODEC = StreamCodec.of(
            (buf, val) -> val.write(buf),
            BinderLocator::read
    );

    public static BinderLocator hand(InteractionHand hand) {
        return new BinderLocator(Type.HAND, "", hand == InteractionHand.MAIN_HAND ? 0 : 1);
    }

    public static BinderLocator accessory(String slotName, int index) {
        return new BinderLocator(Type.ACCESSORY, slotName, index);
    }

    public static BinderLocator inventory(int slotIndex) {
        return new BinderLocator(Type.INVENTORY, "", slotIndex);
    }

    public ItemStack findItem(Player player) {
        switch (type) {
            case HAND:
                InteractionHand hand = index == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                return player.getItemInHand(hand);
            case ACCESSORY:
                return PlatformHelper.INSTANCE.getAccessoryItem(player, slotName, index);
            case INVENTORY:
                if (index >= 0 && index < player.getInventory().getContainerSize()) {
                    return player.getInventory().getItem(index);
                }
                return ItemStack.EMPTY;
            default:
                return ItemStack.EMPTY;
        }
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(type);
        buf.writeUtf(slotName);
        buf.writeInt(index);
    }

    public static BinderLocator read(FriendlyByteBuf buf) {
        Type type = buf.readEnum(Type.class);
        String slotName = buf.readUtf();
        int index = buf.readInt();
        return new BinderLocator(type, slotName, index);
    }
}
