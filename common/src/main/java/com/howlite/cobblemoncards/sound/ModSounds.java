package com.howlite.cobblemoncards.sound;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    public static final SoundEvent BATTLE_NECROZMA = register("music.battle_necrozma");
    public static final SoundEvent BATTLE_PLASMA   = register("music.battle_plasma");
    public static final SoundEvent BATTLE_ZINNIA   = register("music.battle_zinnia");
    public static final SoundEvent CYNTHIA         = register("music.cynthia");
    public static final SoundEvent LITTLEROOT      = register("music.littleroot");
    public static final SoundEvent ROUTE_209       = register("music.route_209");
    public static final SoundEvent SNOWPOINT_CITY  = register("music.snowpoint_city");
    public static final SoundEvent SOUL_HEART      = register("music.soul_heart");

    private static SoundEvent register(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("cobblemon-cards", name);
        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(id);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, soundEvent);
    }

    public static void registerSounds() {
        // Enregistre les sons au chargement de la classe
    }
}
