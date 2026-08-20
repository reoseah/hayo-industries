package hayo.processing_machine.classic;

import hayo.Hayo;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;

public class CompressingRecipe extends ClassicMachineRecipe {
    public CompressingRecipe(Ingredient input, int inputCount, ItemStackTemplate result, float extraResultChance, int processingEnergy) {
        super(input, inputCount, result, extraResultChance, processingEnergy);
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
