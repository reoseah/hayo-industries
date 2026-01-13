package io.github.reoseah.hayo.feature.energy.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public record ChargedAttributes(ItemAttributeModifiers attributes, int requiredEnergy) {
    public static final Codec<ChargedAttributes> CODEC = RecordCodecBuilder.create(instance -> instance //
            .group( //
                    ItemAttributeModifiers.CODEC.fieldOf("attributes").forGetter(ChargedAttributes::attributes), //
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("required_energy").forGetter(ChargedAttributes::requiredEnergy) //
            ).apply(instance, ChargedAttributes::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChargedAttributes> STREAM_CODEC = StreamCodec.composite( //
            ItemAttributeModifiers.STREAM_CODEC, //
            ChargedAttributes::attributes, //
            ByteBufCodecs.VAR_INT, //
            ChargedAttributes::requiredEnergy, //
            ChargedAttributes::new);
}
