package io.github.reoseah.hayo.feature.processing_machines;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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

}
