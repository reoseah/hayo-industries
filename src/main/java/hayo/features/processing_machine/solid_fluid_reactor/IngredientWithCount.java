package hayo.features.processing_machine.solid_fluid_reactor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;

public record IngredientWithCount(Ingredient ingredient, int count) {
    public static final MapCodec<IngredientWithCount> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i
            .group(Ingredient.CODEC.fieldOf("ingredient").forGetter(IngredientWithCount::ingredient),
                    Codec.INT.fieldOf("count").forGetter(IngredientWithCount::count))
            .apply(i, IngredientWithCount::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, IngredientWithCount> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            IngredientWithCount::ingredient,
            ByteBufCodecs.VAR_INT,
            IngredientWithCount::count,
            IngredientWithCount::new
    );
}
