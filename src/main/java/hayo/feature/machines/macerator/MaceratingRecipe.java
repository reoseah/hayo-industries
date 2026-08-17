package hayo.feature.machines.macerator;

import hayo.Hayo;
import hayo.feature.machines.ClassicMachineRecipe;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;

public class MaceratingRecipe extends ClassicMachineRecipe {
    private static final int DEFAULT_DURATION_SECONDS = 10;
    public static final int DEFAULT_ENERGY = DEFAULT_DURATION_SECONDS * MaceratorBlockEntity.ENERGY_USE_RATE * 20;

    public MaceratingRecipe(Ingredient input, int inputCount, ItemStackTemplate result, float extraResultChance, int processingEnergy) {
        super(input, inputCount, result, extraResultChance, processingEnergy);
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
