package com.howlite.cobblemoncards.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record ScanData(int count, List<UUID> scannedPokemon, Optional<String> targetSpecies) {

    public static final Codec<ScanData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("count").forGetter(ScanData::count),
            UUIDUtil.CODEC.listOf().fieldOf("scanned_pokemon").forGetter(ScanData::scannedPokemon),
            Codec.STRING.optionalFieldOf("target_species").forGetter(ScanData::targetSpecies)
    ).apply(instance, ScanData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ScanData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                ByteBufCodecs.VAR_INT.encode(buf, data.count());
                ByteBufCodecs.collection(ArrayList::new, UUIDUtil.STREAM_CODEC).encode(buf, new ArrayList<>(data.scannedPokemon()));
                ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).encode(buf, data.targetSpecies());
            },
            buf -> new ScanData(
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.collection(ArrayList::new, UUIDUtil.STREAM_CODEC).decode(buf),
                    ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).decode(buf)
            )
    );
}