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

public record DiskData(int dust, int scanCount, List<UUID> scannedPokemon, Optional<String> targetSpecies) {

    public static final Codec<DiskData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("dust").forGetter(DiskData::dust),
            Codec.INT.fieldOf("scan_count").forGetter(DiskData::scanCount),
            UUIDUtil.CODEC.listOf().fieldOf("scanned_pokemon").forGetter(DiskData::scannedPokemon),
            Codec.STRING.optionalFieldOf("target_species").forGetter(DiskData::targetSpecies)
    ).apply(instance, DiskData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DiskData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                ByteBufCodecs.VAR_INT.encode(buf, data.dust());
                ByteBufCodecs.VAR_INT.encode(buf, data.scanCount());
                ByteBufCodecs.collection(ArrayList::new, UUIDUtil.STREAM_CODEC).encode(buf, new ArrayList<>(data.scannedPokemon()));
                ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).encode(buf, data.targetSpecies());
            },
            buf -> new DiskData(
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.collection(ArrayList::new, UUIDUtil.STREAM_CODEC).decode(buf),
                    ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).decode(buf)
            )
    );

    public static DiskData empty() {
        return new DiskData(0, 0, new ArrayList<>(), Optional.empty());
    }
}