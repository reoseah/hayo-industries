package io.github.reoseah.hayoind.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.hayoind.Hayo;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;

public record ExtractingRecipe(Ingredient input, //
                               List<ItemStack> results, //
                               int processingEnergy) //
        implements Recipe<SingleRecipeInput> {
    private static final int DEFAULT_ENERGY = 12 * 2 * 20; // 12s at 2e/tick and 20 ticks in second

    public static final MapCodec<ExtractingRecipe> CODEC = RecordCodecBuilder.mapCodec( //
            instance -> instance.group( //
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(ExtractingRecipe::input), //
                    ItemStack.STRICT_CODEC.listOf().fieldOf("results").forGetter(ExtractingRecipe::results), //
                    Codec.INT.fieldOf("processing_energy").orElse(DEFAULT_ENERGY).forGetter(ExtractingRecipe::processingEnergy) //
            ).apply(instance, ExtractingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ExtractingRecipe> STREAM_CODEC = StreamCodec.composite( //
            Ingredient.CONTENTS_STREAM_CODEC, //
            ExtractingRecipe::input, //
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), //
            ExtractingRecipe::results, //
            ByteBufCodecs.INT, //
            ExtractingRecipe::processingEnergy, //
            ExtractingRecipe::new);

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.input.test(input.item());
    }

    /// @deprecated extractor outputs multiple stacks from a recipe, use [#results].
    @Override
    @Deprecated
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return Hayo.RecipeSerializers.EXTRACTING;
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return Hayo.RecipeTypes.EXTRACTING;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public static class Serializer implements RecipeSerializer<ExtractingRecipe> {
        @Override
        public MapCodec<ExtractingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ExtractingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
