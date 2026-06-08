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
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CustomBoosterCommand {

    // Stockage persistant des sessions pour permettre l'aller-retour entre le coffre et l'enclume
    private static final Map<UUID, CreatorSession> SESSIONS = new ConcurrentHashMap<>();

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

    public static class CreatorSession {
        public final List<ItemStack> rewards = new ArrayList<>();
        public int selectedSkinIndex = 0;
        public String customName = "";

        public CreatorSession() {
            for (int i = 0; i < 5; i++) {
                rewards.add(ItemStack.EMPTY);
            }
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cobblecard")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("custombooster")
                .executes(context -> {
                    try {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        // Initialiser ou réinitialiser la session à l'appel de la commande
                        SESSIONS.put(player.getUUID(), new CreatorSession());
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
        CreatorSession session = SESSIONS.computeIfAbsent(player.getUUID(), uuid -> new CreatorSession());

        // Container de 54 slots (9 colonnes x 6 lignes)
        SimpleContainer container = new SimpleContainer(54);

        // Restaurer les items de la session dans les slots 0 à 4
        for (int i = 0; i < 5; i++) {
            container.setItem(i, session.rewards.get(i).copy());
        }

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

        // Bloquer slots de la ligne 2 (9 à 17) sauf le slot 13 (Aperçu) et slot 14 (Renommer)
        for (int i = 9; i <= 17; i++) {
            if (i == 13 || i == 14) continue;
            container.setItem(i, grayGlass.copy());
        }

        // Aperçu de la skin sélectionnée (au slot 13)
        updateSkinPreview(container, BOOSTER_SKINS[session.selectedSkinIndex], session.customName);

        // Bouton de renommage au slot 14 (Name Tag)
        ItemStack renameButton = new ItemStack(Items.NAME_TAG);
        renameButton.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.translatable("gui.cobblemon-cards.custom_booster_creator.rename_button"));
        List<Component> renameLore = new ArrayList<>();
        String displayName = session.customName.isEmpty() ? Component.translatable(BOOSTER_SKINS[session.selectedSkinIndex].getDescriptionId()).getString() : session.customName;
        renameLore.add(Component.translatable("gui.cobblemon-cards.custom_booster_creator.rename_button_lore_1", displayName));
        renameLore.add(Component.translatable("gui.cobblemon-cards.custom_booster_creator.rename_button_lore_2"));
        renameButton.set(net.minecraft.core.component.DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(renameLore));
        container.setItem(14, renameButton);

        // Placer les 28 booster pack skins dans les slots 18 à 45
        for (int i = 0; i < BOOSTER_SKINS.length; i++) {
            ItemStack skinStack = new ItemStack(BOOSTER_SKINS[i]);
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
                private boolean validatedOrRenaming = false;

                @Override
                public void removed(net.minecraft.world.entity.player.Player playerEntity2) {
                    super.removed(playerEntity2);
                    
                    // Si on ne valide pas et qu'on ne renomme pas (fermeture réelle), on vide la session et on rend les items
                    if (!validatedOrRenaming) {
                        for (int i = 0; i < 5; i++) {
                            ItemStack stack = container.getItem(i);
                            if (!stack.isEmpty()) {
                                if (!playerEntity2.getInventory().add(stack)) {
                                    playerEntity2.drop(stack, false);
                                }
                            }
                        }
                        SESSIONS.remove(playerEntity2.getUUID());
                    }
                }

                @Override
                public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int index) {
                    if (index >= 5 && index <= 53) {
                        return ItemStack.EMPTY;
                    }
                    return super.quickMoveStack(player, index);
                }

                @Override
                public void clicked(int slotId, int button, net.minecraft.world.inventory.ClickType clickType, net.minecraft.world.entity.player.Player playerEntity2) {
                    // Bloquer les clics sur les vitres, le slot 13 d'aperçu
                    if ((slotId >= 5 && slotId <= 7) || (slotId >= 9 && slotId <= 12) || slotId == 13 || (slotId >= 15 && slotId <= 17) || (slotId >= 46 && slotId <= 53)) {
                        return;
                    }

                    // Clic sur le bouton de Renommer (slot 14)
                    if (slotId == 14) {
                        // Sauvegarder l'état actuel des slots 0 à 4 dans la session
                        for (int i = 0; i < 5; i++) {
                            session.rewards.set(i, container.getItem(i).copy());
                        }
                        this.validatedOrRenaming = true;
                        
                        // Vider le container pour ne pas dupliquer ou rendre les items lors du changement de menu
                        container.clearContent();
                        
                        // Ouvrir le menu d'enclume pour renommer
                        ((ServerPlayer) playerEntity2).getServer().execute(() -> openRenameGui((ServerPlayer) playerEntity2, session));
                        return;
                    }

                    // Clic sur l'une des 28 skins (slots 18 à 45)
                    if (slotId >= 18 && slotId <= 17 + BOOSTER_SKINS.length) {
                        int index = slotId - 18;
                        if (index >= 0 && index < BOOSTER_SKINS.length) {
                            session.selectedSkinIndex = index;
                            updateSkinPreview(container, BOOSTER_SKINS[index], session.customName);
                            
                            // Mettre à jour également le tooltip du bouton Renommer
                            ItemStack tag = container.getItem(14);
                            if (!tag.isEmpty()) {
                                List<Component> tagLore = new ArrayList<>();
                                String dName = session.customName.isEmpty() ? Component.translatable(BOOSTER_SKINS[index].getDescriptionId()).getString() : session.customName;
                                tagLore.add(Component.translatable("gui.cobblemon-cards.custom_booster_creator.rename_button_lore_1", dName));
                                tagLore.add(Component.translatable("gui.cobblemon-cards.custom_booster_creator.rename_button_lore_2"));
                                tag.set(net.minecraft.core.component.DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(tagLore));
                            }
                            
                            playerEntity2.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
                        }
                        return;
                    }

                    // Clic sur la Nether Star de validation (slot 8)
                    if (slotId == 8) {
                        List<ItemStack> customItems = new ArrayList<>();
                        boolean hasItems = false;
                        
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
                            playerEntity2.sendSystemMessage(Component.translatable("message.cobblemon-cards.custom_booster_creator.no_items"));
                            return;
                        }

                        // Création du booster avec la skin et le nom choisis
                        Item selectedSkinItem = BOOSTER_SKINS[session.selectedSkinIndex];
                        ItemStack boosterStack = new ItemStack(selectedSkinItem);
                        boosterStack.set(ModDataComponents.CUSTOM_BOOSTER_DATA, customItems);
                        
                        // Nom customisé (soit saisi, soit le nom translatable doré du booster custom)
                        if (!session.customName.isEmpty()) {
                            boosterStack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal("§d§l" + session.customName));
                        } else {
                            boosterStack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.translatable("item.cobblemon-cards.custom_booster_pack"));
                        }

                        if (!playerEntity2.getInventory().add(boosterStack)) {
                            playerEntity2.drop(boosterStack, false);
                        }

                        playerEntity2.sendSystemMessage(Component.translatable("message.cobblemon-cards.custom_booster_creator.success"));
                        
                        this.validatedOrRenaming = true;
                        container.clearContent();
                        SESSIONS.remove(playerEntity2.getUUID());
                        ((ServerPlayer) playerEntity2).closeContainer();
                        return;
                    }

                    super.clicked(slotId, button, clickType, playerEntity2);
                }
            };
        }, Component.translatable("gui.cobblemon-cards.custom_booster_creator.title")));
    }

    private static void openRenameGui(ServerPlayer player, CreatorSession session) {
        player.openMenu(new SimpleMenuProvider((containerId, playerInventory, playerEntity) -> {
            AnvilMenu anvilMenu = new AnvilMenu(containerId, playerInventory, ContainerLevelAccess.create(playerEntity.level(), playerEntity.blockPosition())) {
                @Override
                public boolean stillValid(net.minecraft.world.entity.player.Player player) {
                    return true;
                }

                @Override
                public void removed(net.minecraft.world.entity.player.Player playerEntity2) {
                    // Vider les slots de l'enclume avant sa fermeture pour ne pas redonner le Name Tag temporaire
                    this.getSlot(0).set(ItemStack.EMPTY);
                    this.getSlot(1).set(ItemStack.EMPTY);
                    this.getSlot(2).set(ItemStack.EMPTY);
                    super.removed(playerEntity2);

                    // Revenir automatiquement au créateur de booster
                    ((ServerPlayer) playerEntity2).getServer().execute(() -> openCustomBoosterCreator((ServerPlayer) playerEntity2));
                }

                @Override
                public void clicked(int slotId, int button, net.minecraft.world.inventory.ClickType clickType, net.minecraft.world.entity.player.Player playerEntity2) {
                    // Clic sur le slot de sortie validation (slot 2)
                    if (slotId == 2) {
                        ItemStack outputStack = this.getSlot(2).getItem();
                        if (!outputStack.isEmpty()) {
                            String newName = outputStack.getHoverName().getString();
                            session.customName = newName;
                            ((ServerPlayer) playerEntity2).closeContainer(); // Déclenche removed() qui réouvre le menu principal
                        }
                        return;
                    }
                    super.clicked(slotId, button, clickType, playerEntity2);
                }
            };

            // Mettre l'étiquette de départ dans le slot d'entrée gauche de l'enclume
            ItemStack nameTag = new ItemStack(Items.NAME_TAG);
            String currentName = session.customName.isEmpty() ? Component.translatable("item.cobblemon-cards.custom_booster_pack").getString() : session.customName;
            // Supprimer le formattage couleur brut pour la saisie
            if (currentName.startsWith("§d§l")) {
                currentName = currentName.substring(4);
            }
            nameTag.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal(currentName));
            anvilMenu.getSlot(0).set(nameTag);

            return anvilMenu;
        }, Component.translatable("gui.cobblemon-cards.custom_booster_creator.rename_gui_title")));
    }

    private static void updateSkinPreview(SimpleContainer container, Item item, String customName) {
        ItemStack previewStack = new ItemStack(item);
        String nameString = customName.isEmpty() ? Component.translatable(item.getDescriptionId()).getString() : customName;
        previewStack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.translatable(
            "gui.cobblemon-cards.custom_booster_creator.selected_skin", 
            Component.literal(nameString)
        ));
        container.setItem(13, previewStack);
    }
}
