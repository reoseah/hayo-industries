package hayo.energy.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public record EnergyStorage(int capacity, int transferLimit) {
    public static final Codec<EnergyStorage> CODEC = RecordCodecBuilder.create(instance -> instance //
            .group( //
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("capacity").forGetter(EnergyStorage::capacity), //
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("transfer_limit").forGetter(EnergyStorage::transferLimit) //
            ).apply(instance, EnergyStorage::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EnergyStorage> STREAM_CODEC = StreamCodec.composite( //
            ByteBufCodecs.VAR_INT, //
            EnergyStorage::capacity, //
            ByteBufCodecs.VAR_INT, //
            EnergyStorage::transferLimit, //
            EnergyStorage::new);
}
