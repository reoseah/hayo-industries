package hayo.processing_machine.classic;

import hayo.Hayo;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;

public class ExtractingRecipe extends ClassicMachineRecipe {
    private static final int DEFAULT_DURATION_SECONDS = 15;
    public static final int DEFAULT_ENERGY = DEFAULT_DURATION_SECONDS * ExtractorBlockEntity.ENERGY_USE_RATE * 20;

    public ExtractingRecipe(Ingredient input, int inputCount, ItemStackTemplate result, float extraResultChance, int processingEnergy) {
        super(input, inputCount, result, extraResultChance, processingEnergy);
    }

    @Override
    public RecipeSerializer<? extends SingleItemRecipe> getSerializer() {
        return Hayo.RecipeSerializers.EXTRACTING;
    }

    @Override
    public RecipeType<? extends SingleItemRecipe> getType() {
        return Hayo.RecipeTypes.EXTRACTING;
    }
}
