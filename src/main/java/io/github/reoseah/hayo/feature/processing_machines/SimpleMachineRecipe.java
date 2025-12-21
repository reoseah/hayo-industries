package io.github.reoseah.hayo.feature.processing_machines;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.processing_machines.compressor.CompressorBlockEntity;
import io.github.reoseah.hayo.feature.processing_machines.extractor.ExtractorBlockEntity;
import io.github.reoseah.hayo.feature.processing_machines.macerator.MaceratorBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public abstract class SimpleMachineRecipe implements Recipe<SingleRecipeInput>, MachineRecipe {
    private final Ingredient input;
    private final ItemStack result;
    private final int processingEnergy;

    public SimpleMachineRecipe(Ingredient input, //
                               ItemStack result, //
                               int processingEnergy) {
        this.input = input;
        this.result = result;
        this.processingEnergy = processingEnergy;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.input.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public RecipeBookCategory recipeBookCategory() {
        return null;
    }

    public Ingredient input() {
        return this.input;
    }

    public ItemStack result() {
        return this.result;
    }

    @Override
    public int processingEnergy() {
        return this.processingEnergy;
    }

    @FunctionalInterface
    public interface Factory<R extends SimpleMachineRecipe> {
        R create(Ingredient input, ItemStack result, int processingEnergy);
    }

    public static class Serializer<R extends SimpleMachineRecipe> implements RecipeSerializer<R> {
        private final MapCodec<R> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, R> streamCodec;

        public Serializer(Factory<R> factory, int defaultEnergy) {
            this.codec = RecordCodecBuilder.mapCodec( //
                    instance -> instance.group( //
                            Ingredient.CODEC.fieldOf("ingredient").forGetter(SimpleMachineRecipe::input), //
                            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(SimpleMachineRecipe::result), //
                            Codec.INT.fieldOf("processing_energy").orElse(defaultEnergy).forGetter(SimpleMachineRecipe::processingEnergy) //
                    ).apply(instance, factory::create));
            this.streamCodec = StreamCodec.composite( //
                    Ingredient.CONTENTS_STREAM_CODEC, //
                    SimpleMachineRecipe::input, //
                    ItemStack.STREAM_CODEC, //
                    SimpleMachineRecipe::result, //
                    ByteBufCodecs.INT, //
                    SimpleMachineRecipe::processingEnergy, //
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

    public static class Macerating extends SimpleMachineRecipe {
        private static final int DEFAULT_DURATION_SECONDS = 10;
        public static final int DEFAULT_ENERGY = DEFAULT_DURATION_SECONDS * MaceratorBlockEntity.ENERGY_USE_RATE * 20;

        public Macerating(Ingredient input, ItemStack result, int processingEnergy) {
            super(input, result, processingEnergy);
        }

        @Override
        public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
            return Hayo.RecipeSerializers.MACERATING;
        }

        @Override
        public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
            return Hayo.RecipeTypes.MACERATING;
        }
    }

    public static class Compressing extends SimpleMachineRecipe {
        private static final int DEFAULT_DURATION_SECONDS = 12;
        public static final int DEFAULT_ENERGY = DEFAULT_DURATION_SECONDS * CompressorBlockEntity.ENERGY_USE_RATE * 20;

        public Compressing(Ingredient input, ItemStack result, int processingEnergy) {
            super(input, result, processingEnergy);
        }

        @Override
        public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
            return Hayo.RecipeSerializers.COMPRESSING;
        }

        @Override
        public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
            return Hayo.RecipeTypes.COMPRESSING;
        }
    }

    public static class Extracting extends SimpleMachineRecipe {
        private static final int DEFAULT_DURATION_SECONDS = 15;
        public static final int DEFAULT_ENERGY = DEFAULT_DURATION_SECONDS * ExtractorBlockEntity.ENERGY_USE_RATE * 20;

        public Extracting(Ingredient input, ItemStack result, int processingEnergy) {
            super(input, result, processingEnergy);
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
