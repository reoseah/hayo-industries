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

public record MaceratingRecipe(Ingredient input, //
                               ItemStack result, //
                               int processingEnergy) //
        implements Recipe<SingleRecipeInput> {
    private static final int DEFAULT_ENERGY = 10 * 2 * 20; // 15s at 2e/tick and 20 ticks in second

    public static final MapCodec<MaceratingRecipe> CODEC = RecordCodecBuilder.mapCodec( //
            instance -> instance.group( //
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(MaceratingRecipe::input), //
                    ItemStack.STRICT_CODEC.fieldOf("result").forGetter(MaceratingRecipe::result), //
                    Codec.INT.fieldOf("processing_energy").orElse(DEFAULT_ENERGY).forGetter(MaceratingRecipe::processingEnergy) //
            ).apply(instance, MaceratingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MaceratingRecipe> STREAM_CODEC = StreamCodec.composite( //
            Ingredient.CONTENTS_STREAM_CODEC, //
            MaceratingRecipe::input, //
            ItemStack.STREAM_CODEC, //
            MaceratingRecipe::result, //
            ByteBufCodecs.INT, //
            MaceratingRecipe::processingEnergy, //
            MaceratingRecipe::new);

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.input.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return Hayo.RecipeSerializers.MACERATING;
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return Hayo.RecipeTypes.MACERATING;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public static class Serializer implements RecipeSerializer<MaceratingRecipe> {
        @Override
        public MapCodec<MaceratingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MaceratingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
