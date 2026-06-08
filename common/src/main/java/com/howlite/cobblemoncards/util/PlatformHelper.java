package com.howlite.cobblemoncards.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.List;

public interface PlatformHelper {
    
    // Instance globale affectée lors de l'initialisation du mod, utilisant un proxy pour éviter les soucis de bootstrap
    PlatformHelper INSTANCE = (PlatformHelper) java.lang.reflect.Proxy.newProxyInstance(
            PlatformHelper.class.getClassLoader(),
            new Class<?>[]{PlatformHelper.class},
            (proxy, method, args) -> {
                PlatformHelper helper = PlatformHelperHolder.get();
                if (helper == null) {
                    throw new IllegalStateException("PlatformHelper has not been initialized yet!");
                }
                return method.invoke(helper, args);
            }
    );

    class PlatformHelperHolder {
        private static PlatformHelper instance;
        public static void set(PlatformHelper helper) {
            instance = helper;
        }
        public static PlatformHelper get() {
            return instance;
        }
    }

    List<EquippedAccessory> getEquippedAccessories(LivingEntity entity);
    boolean equipItem(Player player, ItemStack stack);
    void refreshEquippedModifiers(LivingEntity entity);

    boolean hasGuaranteedGodPack(Player player);
    void setGuaranteedGodPack(Player player, boolean value);

    int getOpenedBoosters(Player player);
    void setOpenedBoosters(Player player, int value);

    List<String> getDiscoveredCards(Player player);
    void setDiscoveredCards(Player player, List<String> cards);
    void addDiscoveredCard(Player player, String cardId);

    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);
    void sendToServer(CustomPacketPayload payload);

    net.minecraft.world.item.CreativeModeTab createCreativeTab(String name, java.util.function.Supplier<ItemStack> icon, net.minecraft.world.item.CreativeModeTab.DisplayItemsGenerator displayItems);

    com.howlite.cobblemoncards.item.custom.BinderItem createBinderItem(com.howlite.cobblemoncards.item.custom.BinderTier tier, net.minecraft.world.item.Item.Properties properties);
    com.howlite.cobblemoncards.item.custom.MasterAlbumItem createMasterAlbumItem(net.minecraft.world.item.Item.Properties properties);
    com.howlite.cobblemoncards.item.custom.CardItem createCardItem(net.minecraft.world.item.Item.Properties properties);

    default boolean isNeoForge() {
        return false;
    }
}
