package com.howlite.cobblemoncards.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class HoloProjectorBlockEntity extends BlockEntity implements ImplementedInventory {
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
    private int displayMode = 0; // 0=Turn, 1=Face Player, 2=Dynamic, 3=Fixed, 4=Horizontal, 5=Bobbing Only
    private int musicCooldown = 0;
    private boolean isPlayingMusic = false;
    private String lastPlayedSongKey = "";

    public HoloProjectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HOLO_PROJECTOR_BE, pos, state);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return inventory;
    }

    public int getDisplayMode() {
        return displayMode;
    }

    public void cycleDisplayMode() {
        this.displayMode = (this.displayMode + 1) % 6; // Augmenté à 6 modes
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, inventory, registries);
        tag.putInt("DisplayMode", displayMode);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.clear();
        ContainerHelper.loadAllItems(tag, inventory, registries);
        if (tag.contains("DisplayMode")) {
            displayMode = tag.getInt("DisplayMode");
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, HoloProjectorBlockEntity blockEntity) {
        if (blockEntity.musicCooldown > 0) {
            blockEntity.musicCooldown--;
            if (blockEntity.musicCooldown <= 0) {
                blockEntity.isPlayingMusic = false;
            }
        }

        if (!level.isClientSide()) {
            boolean isJukeboxBelow = level.getBlockState(pos.below()).is(net.minecraft.world.level.block.Blocks.JUKEBOX);
            ItemStack card = blockEntity.getItem(0);
            boolean hasCard = !card.isEmpty() && card.getItem() instanceof com.howlite.cobblemoncards.item.custom.CardItem;

            boolean shouldPlay = isJukeboxBelow && hasCard;

            if (shouldPlay) {
                SoundEvent track = selectPokemonMusic(card);
                String songKey = track.getLocation().toString();

                if (!blockEntity.isPlayingMusic || !blockEntity.lastPlayedSongKey.equals(songKey)) {
                    // Si une autre musique jouait déjà à cet endroit, on l'arrête d'abord
                    if (blockEntity.isPlayingMusic) {
                        stopAllPokemonMusic(level, pos);
                    }

                    // Démarrer la musique sur le Jukebox !
                    level.playSound(null, pos.below(), track, SoundSource.RECORDS, 1.5f, 1.0f);
                    blockEntity.isPlayingMusic = true;
                    blockEntity.lastPlayedSongKey = songKey;

                    // Cooldown précis selon la piste (approximatif en ticks)
                    int duration = 1500; // défaut
                    if (track == com.howlite.cobblemoncards.sound.ModSounds.SOUL_HEART) {
                        duration = 2800;
                    } else if (track == com.howlite.cobblemoncards.sound.ModSounds.BATTLE_NECROZMA) {
                        duration = 2200;
                    } else if (track == com.howlite.cobblemoncards.sound.ModSounds.BATTLE_ZINNIA) {
                        duration = 2000;
                    } else if (track == com.howlite.cobblemoncards.sound.ModSounds.BATTLE_PLASMA) {
                        duration = 1900;
                    } else if (track == com.howlite.cobblemoncards.sound.ModSounds.CYNTHIA) {
                        duration = 1400;
                    } else if (track == com.howlite.cobblemoncards.sound.ModSounds.LITTLEROOT) {
                        duration = 1600;
                    } else if (track == com.howlite.cobblemoncards.sound.ModSounds.ROUTE_209) {
                        duration = 1300;
                    } else if (track == com.howlite.cobblemoncards.sound.ModSounds.SNOWPOINT_CITY) {
                        duration = 1700;
                    }
                    blockEntity.musicCooldown = duration;

                    // Afficher le message d'action bar style Minecraft Jukebox
                    Component songName = Component.translatable("music.cobblemon-cards." + track.getLocation().getPath());
                    Component message = Component.translatable("record.nowPlaying", songName);

                    double x = pos.getX();
                    double y = pos.getY();
                    double z = pos.getZ();
                    AABB aabb = new AABB(x - 32, y - 32, z - 32, x + 32, y + 32, z + 32);
                    for (Player player : level.getEntitiesOfClass(Player.class, aabb)) {
                        player.displayClientMessage(message, true);
                    }
                }
            } else {
                // Arrêt de la musique si le Jukebox est brisé ou que la carte est retirée
                if (blockEntity.isPlayingMusic) {
                    stopAllPokemonMusic(level, pos);
                    blockEntity.isPlayingMusic = false;
                    blockEntity.lastPlayedSongKey = "";
                    blockEntity.musicCooldown = 0;
                }
            }
        }
    }

    private static SoundEvent selectPokemonMusic(ItemStack card) {
        com.howlite.cobblemoncards.component.CardData cardData = card.get(com.howlite.cobblemoncards.component.ModDataComponents.CARD_DATA);
        if (cardData != null) {
            String rarity = cardData.rarity().toLowerCase();
            // Mythic / Legendary + shiny → épiques de combat
            if (rarity.equalsIgnoreCase("mythic")) {
                return com.howlite.cobblemoncards.sound.ModSounds.SOUL_HEART;
            }
            if (rarity.equalsIgnoreCase("legendary") && cardData.isShiny()) {
                return com.howlite.cobblemoncards.sound.ModSounds.BATTLE_NECROZMA;
            }
            if (rarity.equalsIgnoreCase("legendary")) {
                return com.howlite.cobblemoncards.sound.ModSounds.CYNTHIA;
            }
            if (cardData.isShiny()) {
                return com.howlite.cobblemoncards.sound.ModSounds.BATTLE_ZINNIA;
            }
            // Sélection par stat
            String statName = cardData.stat().getSerializedName().toLowerCase();
            if (statName.contains("fire") || statName.contains("attack")) {
                return com.howlite.cobblemoncards.sound.ModSounds.BATTLE_PLASMA;
            } else if (statName.contains("water") || statName.contains("speed")) {
                return com.howlite.cobblemoncards.sound.ModSounds.ROUTE_209;
            } else if (statName.contains("grass") || statName.contains("health")) {
                return com.howlite.cobblemoncards.sound.ModSounds.LITTLEROOT;
            } else if (statName.contains("ice") || statName.contains("armor")) {
                return com.howlite.cobblemoncards.sound.ModSounds.SNOWPOINT_CITY;
            }
        }
        return com.howlite.cobblemoncards.sound.ModSounds.LITTLEROOT;
    }

    public static void stopAllPokemonMusic(Level level, BlockPos pos) {
        if (level.isClientSide()) return;
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        AABB aabb = new AABB(x - 32, y - 32, z - 32, x + 32, y + 32, z + 32);

        net.minecraft.resources.ResourceLocation[] tracks = new net.minecraft.resources.ResourceLocation[]{
            com.howlite.cobblemoncards.sound.ModSounds.BATTLE_NECROZMA.getLocation(),
            com.howlite.cobblemoncards.sound.ModSounds.BATTLE_PLASMA.getLocation(),
            com.howlite.cobblemoncards.sound.ModSounds.BATTLE_ZINNIA.getLocation(),
            com.howlite.cobblemoncards.sound.ModSounds.CYNTHIA.getLocation(),
            com.howlite.cobblemoncards.sound.ModSounds.LITTLEROOT.getLocation(),
            com.howlite.cobblemoncards.sound.ModSounds.ROUTE_209.getLocation(),
            com.howlite.cobblemoncards.sound.ModSounds.SNOWPOINT_CITY.getLocation(),
            com.howlite.cobblemoncards.sound.ModSounds.SOUL_HEART.getLocation()
        };

        for (Player player : level.getEntitiesOfClass(Player.class, aabb)) {
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                for (net.minecraft.resources.ResourceLocation track : tracks) {
                    serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(track, SoundSource.RECORDS));
                }
            }
        }

        // Envoyer également les événements de niveau pour arrêter les disques vanilla physiques à ces positions
        level.levelEvent(null, 1011, pos.below(), 0);
        level.levelEvent(null, 1011, pos, 0);
    }
}
