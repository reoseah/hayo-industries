package io.github.reoseah.hayo.feature.processing_machines.extractor;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.processing_machines.ClassicMachineRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;

public class ExtractingRecipe extends ClassicMachineRecipe {
    private static final int DEFAULT_DURATION_SECONDS = 15;
    public static final int DEFAULT_ENERGY = DEFAULT_DURATION_SECONDS * ExtractorBlockEntity.ENERGY_USE_RATE * 20;

    public ExtractingRecipe(Ingredient input, ItemStackTemplate result, int processingEnergy, float extraChance) {
        super(input, result, processingEnergy, extraChance);
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
