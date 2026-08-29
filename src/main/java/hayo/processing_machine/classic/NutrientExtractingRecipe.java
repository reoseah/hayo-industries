package hayo.processing_machine.classic;

import hayo.Hayo;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleItemRecipe;

public class NutrientExtractingRecipe extends ClassicMachineRecipe {
    public NutrientExtractingRecipe(Ingredient input, int inputCount, ItemStackTemplate result, float extraResultChance, int processingEnergy) {
        super(input, inputCount, result, extraResultChance, processingEnergy);
    }

    @Override
    public RecipeSerializer<? extends SingleItemRecipe> getSerializer() {
        return Hayo.RecipeSerializers.NUTRIENT_EXTRACTING;
    }

    @Override
    public RecipeType<? extends SingleItemRecipe> getType() {
        return Hayo.RecipeTypes.NUTRIENT_EXTRACTING;
    }
}
