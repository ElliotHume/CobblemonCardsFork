package com.howlite.cobblemoncards.command;

import com.howlite.cobblemoncards.component.ModDataComponents;
import com.howlite.cobblemoncards.item.ModItems;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class CustomBoosterCommand {

    private static final Item[] BOOSTER_SKINS = new Item[]{
        ModItems.BOOSTER_PACK,
        ModItems.BOOSTER_PACK_GEN1,
        ModItems.BOOSTER_PACK_GEN2,
        ModItems.BOOSTER_PACK_GEN3,
        ModItems.BOOSTER_PACK_GEN4,
        ModItems.BOOSTER_PACK_GEN5,
        ModItems.BOOSTER_PACK_GEN6,
        ModItems.BOOSTER_PACK_GEN7,
        ModItems.BOOSTER_PACK_GEN8,
        ModItems.BOOSTER_PACK_GEN9,
        
        ModItems.BOOSTER_PACK_NORMAL,
        ModItems.BOOSTER_PACK_FIRE,
        ModItems.BOOSTER_PACK_WATER,
        ModItems.BOOSTER_PACK_GRASS,
        ModItems.BOOSTER_PACK_ELECTRIC,
        ModItems.BOOSTER_PACK_ICE,
        ModItems.BOOSTER_PACK_FIGHTING,
        ModItems.BOOSTER_PACK_POISON,
        ModItems.BOOSTER_PACK_GROUND,
        ModItems.BOOSTER_PACK_FLYING,
        ModItems.BOOSTER_PACK_PSYCHIC,
        ModItems.BOOSTER_PACK_BUG,
        ModItems.BOOSTER_PACK_ROCK,
        ModItems.BOOSTER_PACK_GHOST,
        ModItems.BOOSTER_PACK_DRAGON,
        ModItems.BOOSTER_PACK_STEEL,
        ModItems.BOOSTER_PACK_FAIRY,
        ModItems.BOOSTER_PACK_DARK
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cobblecard")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("custombooster")
                .executes(context -> {
                    try {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        openCustomBoosterCreator(player);
                        return 1;
                    } catch (Exception e) {
                        context.getSource().sendFailure(Component.translatable("message.cobblemon-cards.custom_booster_creator.no_permission"));
                        return 0;
                    }
                })
            )
        );
    }

    private static void openCustomBoosterCreator(ServerPlayer player) {
        // Container de 54 slots (9 colonnes x 6 lignes)
        SimpleContainer container = new SimpleContainer(54);

        // Remplir les fillers (vitres grises) pour les lignes supérieures et latérales
        ItemStack grayGlass = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        grayGlass.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.translatable("gui.cobblemon-cards.custom_booster_creator.blocked"));
        
        // Bloquer slots de la ligne 1 (5 à 7)
        container.setItem(5, grayGlass);
        container.setItem(6, grayGlass.copy());
        container.setItem(7, grayGlass.copy());

        // Bouton de confirmation au slot 8 (Nether Star)
        ItemStack confirmButton = new ItemStack(Items.NETHER_STAR);
        confirmButton.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.translatable("gui.cobblemon-cards.custom_booster_creator.confirm"));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.translatable("gui.cobblemon-cards.custom_booster_creator.lore_line_1"));
        lore.add(Component.translatable("gui.cobblemon-cards.custom_booster_creator.lore_line_2"));
        lore.add(Component.translatable("gui.cobblemon-cards.custom_booster_creator.lore_line_3"));
        confirmButton.set(net.minecraft.core.component.DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(lore));
        container.setItem(8, confirmButton);

        // Bloquer slots de la ligne 2 (9 à 17) sauf le slot 13 (Aperçu de la Skin)
        for (int i = 9; i <= 17; i++) {
            if (i == 13) continue;
            container.setItem(i, grayGlass.copy());
        }

        // Aperçu de la skin initialisé sur le Booster de base
        updateSkinPreview(container, BOOSTER_SKINS[0]);

        // Placer les 28 booster pack skins dans les slots 18 à 45
        for (int i = 0; i < BOOSTER_SKINS.length; i++) {
            ItemStack skinStack = new ItemStack(BOOSTER_SKINS[i]);
            // Ajouter un tooltip informatif sur chaque variante cliquable
            List<Component> skinLore = new ArrayList<>();
            skinLore.add(Component.translatable("gui.cobblemon-cards.custom_booster_creator.select_skin_tooltip"));
            skinStack.set(net.minecraft.core.component.DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(skinLore));
            container.setItem(18 + i, skinStack);
        }

        // Remplir les slots restants de la ligne 6 (46 à 53)
        for (int i = 46; i <= 53; i++) {
            container.setItem(i, grayGlass.copy());
        }

        player.openMenu(new SimpleMenuProvider((containerId, playerInventory, playerEntity) -> {
            return new ChestMenu(MenuType.GENERIC_9x6, containerId, playerInventory, container, 6) {
                // Suivre l'index de la skin sélectionnée
                private int selectedSkinIndex = 0;

                @Override
                public void removed(net.minecraft.world.entity.player.Player player) {
                    super.removed(player);
                    // Rendre les items des 5 premiers slots si fermeture sans validation
                    for (int i = 0; i < 5; i++) {
                        ItemStack stack = container.getItem(i);
                        if (!stack.isEmpty()) {
                            if (!player.getInventory().add(stack)) {
                                player.drop(stack, false);
                            }
                        }
                    }
                }

                @Override
                public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int index) {
                    // Empêcher le déplacement rapide pour tous les slots en dehors de 0-4
                    if (index >= 5 && index <= 53) {
                        return ItemStack.EMPTY;
                    }
                    return super.quickMoveStack(player, index);
                }

                @Override
                public void clicked(int slotId, int button, net.minecraft.world.inventory.ClickType clickType, net.minecraft.world.entity.player.Player playerEntity) {
                    // Empêcher toute interaction avec les slots bloqués / fillers et l'aperçu
                    if ((slotId >= 5 && slotId <= 7) || (slotId >= 9 && slotId <= 12) || slotId == 13 || (slotId >= 14 && slotId <= 17) || (slotId >= 46 && slotId <= 53)) {
                        return;
                    }

                    // Clic sur l'une des 28 skins (slots 18 à 45)
                    if (slotId >= 18 && slotId <= 17 + BOOSTER_SKINS.length) {
                        int index = slotId - 18;
                        if (index >= 0 && index < BOOSTER_SKINS.length) {
                            this.selectedSkinIndex = index;
                            updateSkinPreview(container, BOOSTER_SKINS[index]);
                            // Jouer un petit clic agréable
                            playerEntity.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
                        }
                        return;
                    }

                    // Clic sur la Nether Star de validation (slot 8)
                    if (slotId == 8) {
                        List<ItemStack> customItems = new ArrayList<>();
                        boolean hasItems = false;
                        
                        // Récupérer les items des slots 0 à 4
                        for (int i = 0; i < 5; i++) {
                            ItemStack stack = container.getItem(i);
                            if (!stack.isEmpty()) {
                                customItems.add(stack.copy());
                                hasItems = true;
                            } else {
                                customItems.add(ItemStack.EMPTY);
                            }
                        }

                        if (!hasItems) {
                            playerEntity.sendSystemMessage(Component.translatable("message.cobblemon-cards.custom_booster_creator.no_items"));
                            return;
                        }

                        // Création du booster pack avec l'item sélectionné
                        Item selectedSkinItem = BOOSTER_SKINS[this.selectedSkinIndex];
                        ItemStack boosterStack = new ItemStack(selectedSkinItem);
                        boosterStack.set(ModDataComponents.CUSTOM_BOOSTER_DATA, customItems);
                        
                        // Nom customisé translatable
                        boosterStack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.translatable("item.cobblemon-cards.custom_booster_pack"));

                        // Donner le booster au joueur
                        if (!playerEntity.getInventory().add(boosterStack)) {
                            playerEntity.drop(boosterStack, false);
                        }

                        playerEntity.sendSystemMessage(Component.translatable("message.cobblemon-cards.custom_booster_creator.success"));
                        
                        // Vider le conteneur pour ne pas rendre les items à la fermeture dans removed()
                        container.clearContent();
                        
                        // Fermer le menu
                        playerEntity.closeContainer();
                        return;
                    }

                    super.clicked(slotId, button, clickType, playerEntity);
                }
            };
        }, Component.translatable("gui.cobblemon-cards.custom_booster_creator.title")));
    }

    private static void updateSkinPreview(SimpleContainer container, Item item) {
        ItemStack previewStack = new ItemStack(item);
        previewStack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.translatable(
            "gui.cobblemon-cards.custom_booster_creator.selected_skin", 
            Component.translatable(item.getDescriptionId())
        ));
        container.setItem(13, previewStack);
    }
}
