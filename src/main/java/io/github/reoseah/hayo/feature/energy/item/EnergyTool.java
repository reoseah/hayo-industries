package io.github.reoseah.hayo.feature.energy.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public record EnergyTool(float chargedMiningSpeed, int miningEnergy, int attackEnergy) {
    public static final Codec<EnergyTool> CODEC = RecordCodecBuilder.create(instance -> instance //
            .group( //
                    ExtraCodecs.NON_NEGATIVE_FLOAT.fieldOf("charged_mining_speed").forGetter(EnergyTool::chargedMiningSpeed), //
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("mining_energy").forGetter(EnergyTool::miningEnergy), //
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("attack_energy").forGetter(EnergyTool::attackEnergy) //
            ).apply(instance, EnergyTool::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EnergyTool> STREAM_CODEC = StreamCodec.composite( //
            ByteBufCodecs.FLOAT, //
            EnergyTool::chargedMiningSpeed, //
            ByteBufCodecs.VAR_INT, //
            EnergyTool::miningEnergy, //
            ByteBufCodecs.VAR_INT, //
            EnergyTool::attackEnergy, //
            EnergyTool::new);
}
