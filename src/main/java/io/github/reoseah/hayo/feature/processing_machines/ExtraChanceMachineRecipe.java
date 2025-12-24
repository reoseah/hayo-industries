package io.github.reoseah.hayo.feature.processing_machines;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public abstract class ExtraChanceMachineRecipe extends SimpleMachineRecipe {
    @Getter
    public final float extraChance;

    public ExtraChanceMachineRecipe(Ingredient input, ItemStack result, int processingEnergy, float extraChance) {
        super(input, result, processingEnergy);
        this.extraChance = extraChance;
    }

    @FunctionalInterface
    public interface Factory<R extends ExtraChanceMachineRecipe> {
        R create(Ingredient input, ItemStack result, int processingEnergy, float extraChance);
    }

    public static class Serializer<R extends ExtraChanceMachineRecipe> implements RecipeSerializer<R> {
        private final MapCodec<R> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, R> streamCodec;

        public Serializer(ExtraChanceMachineRecipe.Factory<R> factory, int defaultEnergy) {
            this.codec = RecordCodecBuilder.mapCodec( //
                    instance -> instance.group( //
                            Ingredient.CODEC.fieldOf("ingredient").forGetter(ExtraChanceMachineRecipe::input), //
                            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ExtraChanceMachineRecipe::result), //
                            Codec.INT.fieldOf("processing_energy").orElse(defaultEnergy).forGetter(ExtraChanceMachineRecipe::processingEnergy), //
                            Codec.FLOAT.fieldOf("extra_chance").orElse(0F).forGetter(ExtraChanceMachineRecipe::getExtraChance) //
                    ).apply(instance, factory::create));
            this.streamCodec = StreamCodec.composite( //
                    Ingredient.CONTENTS_STREAM_CODEC, //
                    ExtraChanceMachineRecipe::input, //
                    ItemStack.STREAM_CODEC, //
                    ExtraChanceMachineRecipe::result, //
                    ByteBufCodecs.INT, //
                    ExtraChanceMachineRecipe::processingEnergy, //
                    ByteBufCodecs.FLOAT, //
                    ExtraChanceMachineRecipe::getExtraChance, //
                    factory::create);
        }

        @Override
        public MapCodec<R> codec() {
            return this.codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, R> streamCodec() {
            return this.streamCodec;
        }
    }
}
