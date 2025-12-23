package io.github.reoseah.hayo.feature.processing_machines.extractor;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.processing_machines.SimpleMachineRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

public class ExtractingRecipe extends SimpleMachineRecipe {
    private static final int DEFAULT_DURATION_SECONDS = 15;
    public static final int DEFAULT_ENERGY = DEFAULT_DURATION_SECONDS * ExtractorBlockEntity.ENERGY_USE_RATE * 20;

    public ExtractingRecipe(Ingredient input, ItemStack result, int processingEnergy) {
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
