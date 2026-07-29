package io.github.reoseah.hayo.feature.electric_items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public record EnergyTool(float destroySpeedOverride, int destroyEnergy, int attackEnergy) {
    public static final Codec<EnergyTool> CODEC = RecordCodecBuilder.create(instance -> instance //
            .group( //
                    ExtraCodecs.NON_NEGATIVE_FLOAT.fieldOf("destroy_speed_override").forGetter(EnergyTool::destroySpeedOverride), //
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("destroy_energy").forGetter(EnergyTool::destroyEnergy), //
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("attack_energy").forGetter(EnergyTool::attackEnergy) //
            ).apply(instance, EnergyTool::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EnergyTool> STREAM_CODEC = StreamCodec.composite( //
            ByteBufCodecs.FLOAT, //
            EnergyTool::destroySpeedOverride, //
            ByteBufCodecs.VAR_INT, //
            EnergyTool::destroyEnergy, //
            ByteBufCodecs.VAR_INT, //
            EnergyTool::attackEnergy, //
            EnergyTool::new);
}
