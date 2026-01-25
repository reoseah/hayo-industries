package io.github.reoseah.hayo.feature.processing_machines.macerator;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.processing_machines.ClassicMachineRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;

public class MaceratingRecipe extends ClassicMachineRecipe {
    private static final int DEFAULT_DURATION_SECONDS = 10;
    public static final int DEFAULT_ENERGY = DEFAULT_DURATION_SECONDS * MaceratorBlockEntity.ENERGY_USE_RATE * 20;

    public MaceratingRecipe(Ingredient input, ItemStackTemplate result, int processingEnergy, float extraChance) {
        super(input, result, processingEnergy, extraChance);
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
