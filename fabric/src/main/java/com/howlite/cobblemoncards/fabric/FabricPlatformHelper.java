package com.howlite.cobblemoncards.fabric;

import com.howlite.cobblemoncards.util.PlatformHelper;
import com.howlite.cobblemoncards.util.EquippedAccessory;
import com.howlite.cobblemoncards.attachment.PlayerDataAttachments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Supplier;

public class FabricPlatformHelper implements PlatformHelper {

    @Override
    public List<EquippedAccessory> getEquippedAccessories(LivingEntity entity) {
        List<EquippedAccessory> list = new ArrayList<>();
        var componentOpt = dev.emi.trinkets.api.TrinketsApi.getTrinketComponent(entity);
        if (componentOpt.isPresent()) {
            var component = componentOpt.get();
            component.forEach((slotReference, itemStack) -> {
                if (itemStack != null && !itemStack.isEmpty()) {
                    list.add(new EquippedAccessory(itemStack, slotReference.inventory().getSlotType().getName()));
                }
            });
        }
        return list;
    }

    @Override
    public boolean equipItem(Player player, ItemStack stack) {
        return dev.emi.trinkets.api.TrinketItem.equipItem(player, stack);
    }

    @Override
    public void refreshEquippedModifiers(LivingEntity entity) {
        com.howlite.cobblemoncards.CobblemonCards.LOGGER.info("CobblemonCards: FabricPlatformHelper.refreshEquippedModifiers called for " + entity.getName().getString());
        dev.emi.trinkets.api.TrinketsApi.getTrinketComponent(entity).ifPresent(component -> {
            component.clearCachedModifiers();
            component.update();
        });
    }

    @Override
    public boolean hasGuaranteedGodPack(Player player) {
        return player.getAttachedOrCreate(PlayerDataAttachments.HAS_GUARANTEED_GOD_PACK, () -> false);
    }

    @Override
    public void setGuaranteedGodPack(Player player, boolean value) {
        player.setAttached(PlayerDataAttachments.HAS_GUARANTEED_GOD_PACK, value);
    }

    @Override
    public int getOpenedBoosters(Player player) {
        return player.getAttachedOrCreate(PlayerDataAttachments.OPENED_BOOSTERS, () -> 0);
    }

    @Override
    public void setOpenedBoosters(Player player, int value) {
        player.setAttached(PlayerDataAttachments.OPENED_BOOSTERS, value);
    }

    @Override
    public List<String> getDiscoveredCards(Player player) {
        return player.getAttachedOrCreate(PlayerDataAttachments.DISCOVERED_CARDS, ArrayList::new);
    }

    @Override
    public void setDiscoveredCards(Player player, List<String> cards) {
        player.setAttached(PlayerDataAttachments.DISCOVERED_CARDS, cards);
    }

    @Override
    public void addDiscoveredCard(Player player, String cardId) {
        List<String> list = new ArrayList<>(getDiscoveredCards(player));
        if (!list.contains(cardId)) {
            list.add(cardId);
            setDiscoveredCards(player, list);
        }
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        try {
            Class<?> clientHelper = Class.forName("com.howlite.cobblemoncards.fabric.FabricPlatformHelperClient");
            java.lang.reflect.Method method = clientHelper.getMethod("sendToServer", CustomPacketPayload.class);
            method.invoke(null, payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public CreativeModeTab createCreativeTab(String name, Supplier<ItemStack> icon, CreativeModeTab.DisplayItemsGenerator displayItems) {
        CreativeModeTab tab = net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup.builder()
                .title(Component.translatable("itemGroup.cobblemon-cards." + name))
                .icon(icon)
                .displayItems(displayItems)
                .build();
        return Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, 
                ResourceLocation.fromNamespaceAndPath("cobblemon-cards", name), tab);
    }

    @Override
    public com.howlite.cobblemoncards.item.custom.BinderItem createBinderItem(com.howlite.cobblemoncards.item.custom.BinderTier tier, net.minecraft.world.item.Item.Properties properties) {
        return new com.howlite.cobblemoncards.fabric.item.FabricBinderItem(tier, properties);
    }

    @Override
    public com.howlite.cobblemoncards.item.custom.MasterAlbumItem createMasterAlbumItem(net.minecraft.world.item.Item.Properties properties) {
        return new com.howlite.cobblemoncards.fabric.item.FabricMasterAlbumItem(properties);
    }

    @Override
    public com.howlite.cobblemoncards.item.custom.CardItem createCardItem(net.minecraft.world.item.Item.Properties properties) {
        return new com.howlite.cobblemoncards.item.custom.CardItem(properties);
    }
}
