package com.howlite.cobblemoncards.component;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;

public enum CardStat implements StringRepresentable {
    // Stats Utilitaires / Joueur
    MINING_SPEED("mining_speed"),
    MOVEMENT_SPEED("movement_speed"),
    ATTACK_DAMAGE("attack_damage"),
    ATTACK_SPEED("attack_speed"),
    LUCK("luck"),
    ARMOR("armor"),
    MAX_HEALTH("max_health"),
    CARD_DROP_CHANCE("card_drop_chance"),

    // Stats de Spawn Cobblemon (Boost de rencontre)
    NORMAL_SPAWN("normal_spawn"),
    FIRE_SPAWN("fire_spawn"),
    WATER_SPAWN("water_spawn"),
    GRASS_SPAWN("grass_spawn"),
    ELECTRIC_SPAWN("electric_spawn"),
    ICE_SPAWN("ice_spawn"),
    FIGHTING_SPAWN("fighting_spawn"),
    POISON_SPAWN("poison_spawn"),
    GROUND_SPAWN("ground_spawn"),
    FLYING_SPAWN("flying_spawn"),
    PSYCHIC_SPAWN("psychic_spawn"),
    BUG_SPAWN("bug_spawn"),
    ROCK_SPAWN("rock_spawn"),
    GHOST_SPAWN("ghost_spawn"),
    DRAGON_SPAWN("dragon_spawn"),
    STEEL_SPAWN("steel_spawn"),
    FAIRY_SPAWN("fairy_spawn"),
    DARK_SPAWN("dark_spawn");

    public static final Codec<CardStat> CODEC = StringRepresentable.fromEnum(CardStat::values);

    private final String name;

    CardStat(String name) {
        this.name = name;
    }

    public MutableComponent getTranslatedName() {
        return Component.translatable("stat.cobblemon-cards." + this.name);
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}