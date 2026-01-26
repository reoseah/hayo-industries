package io.github.reoseah.hayo.feature.energy.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public record EnergyArmor(int energyPerDamage) {
    public static final Codec<EnergyArmor> CODEC = RecordCodecBuilder.create(instance -> instance //
            .group( //
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("energy_per_damage").forGetter(EnergyArmor::energyPerDamage) //
            ).apply(instance, EnergyArmor::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EnergyArmor> STREAM_CODEC = StreamCodec.composite( //
            ByteBufCodecs.VAR_INT, //
            EnergyArmor::energyPerDamage, //
            EnergyArmor::new);
}
