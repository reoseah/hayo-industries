package io.github.reoseah.hayo.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.block.entity.ExtractorBlockEntity;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

public abstract class SecondaryOutputElectricRecipe extends SimpleElectricRecipe {
    @Getter
    private final ItemStack secondaryResult;
    @Getter
    private final float secondaryResultChance;

    public SecondaryOutputElectricRecipe(Ingredient input, ItemStack result, int processingEnergy, ItemStack secondaryResult, float secondaryResultChance) {
        super(input, result, processingEnergy);
        this.secondaryResult = secondaryResult;
        this.secondaryResultChance = secondaryResultChance;
    }

    @Override
    @Deprecated
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return super.assemble(input, registries);
    }


    @FunctionalInterface
    public interface Factory<R extends SimpleElectricRecipe> {
        R create(Ingredient input, ItemStack result, int processingEnergy, ItemStack secondaryResult, float secondaryResultChance);
    }

    public static class Serializer<R extends SecondaryOutputElectricRecipe> implements RecipeSerializer<R> {
        private final MapCodec<R> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, R> streamCodec;

        public Serializer(Factory<R> factory, int defaultEnergy) {
            this.codec = RecordCodecBuilder.mapCodec( //
                    instance -> instance.group( //
                            Ingredient.CODEC.fieldOf("ingredient").forGetter(SecondaryOutputElectricRecipe::input), //
                            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(SecondaryOutputElectricRecipe::result), //
                            Codec.INT.fieldOf("processing_energy").orElse(defaultEnergy).forGetter(SecondaryOutputElectricRecipe::processingEnergy), //
                            ItemStack.STRICT_CODEC.fieldOf("secondary_result").orElse(ItemStack.EMPTY).forGetter(SecondaryOutputElectricRecipe::getSecondaryResult), //
                            Codec.FLOAT.fieldOf("secondary_result_chance").orElse(1F).forGetter(SecondaryOutputElectricRecipe::getSecondaryResultChance) //
                    ).apply(instance, factory::create));

            this.streamCodec = StreamCodec.composite( //
                    Ingredient.CONTENTS_STREAM_CODEC, //
                    SecondaryOutputElectricRecipe::input, //
                    ItemStack.STREAM_CODEC, //
                    SecondaryOutputElectricRecipe::result, //
                    ByteBufCodecs.INT, //
                    SecondaryOutputElectricRecipe::processingEnergy, //
                    ItemStack.STREAM_CODEC, //
                    SecondaryOutputElectricRecipe::getSecondaryResult, //
                    ByteBufCodecs.FLOAT, //
                    SecondaryOutputElectricRecipe::getSecondaryResultChance, //
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

    public static class Extracting extends SecondaryOutputElectricRecipe {
        private static final int DEFAULT_DURATION_SECONDS = 15;
        public static final int DEFAULT_ENERGY = DEFAULT_DURATION_SECONDS * ExtractorBlockEntity.ENERGY_USE_RATE * 20;

        public Extracting(Ingredient input, ItemStack result, int processingEnergy, ItemStack secondaryResult, float secondaryResultChance) {
            super(input, result, processingEnergy, secondaryResult, secondaryResultChance);
        }

        @Override
        public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
            return Hayo.RecipeSerializers.EXTRACTING;
        }

        @Override
        public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
            return Hayo.RecipeTypes.EXTRACTING;
        }
    }
}
