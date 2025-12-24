package io.github.reoseah.hayo.feature.processing_machines.compressor;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.processing_machines.SideProductMachineRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

public class CompressingRecipe extends SideProductMachineRecipe {
    private static final int DEFAULT_DURATION_SECONDS = 12;
    public static final int DEFAULT_ENERGY = DEFAULT_DURATION_SECONDS * CompressorBlockEntity.ENERGY_USE_RATE * 20;

    public CompressingRecipe(Ingredient input, ItemStack result, int processingEnergy, ItemStack secondaryResult, float secondaryResultChance) {
        super(input, result, processingEnergy, secondaryResult, secondaryResultChance);
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
