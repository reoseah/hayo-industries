package hayo.processing_machine.classic;

import hayo.Hayo;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleItemRecipe;

public class CompressingRecipe extends ClassicMachineRecipe {
    private static final int DEFAULT_DURATION_SECONDS = CompressorBlockEntity.DEFAULT_RECIPE_DURATION;
    public static final int DEFAULT_ENERGY = DEFAULT_DURATION_SECONDS * CompressorBlockEntity.ENERGY_USE_RATE * 20;

    public CompressingRecipe(Ingredient input, int inputCount, ItemStackTemplate result, float extraResultChance, int processingEnergy) {
        super(input, inputCount, result, extraResultChance, processingEnergy);
    }

    @Override
    public RecipeSerializer<? extends SingleItemRecipe> getSerializer() {
        return Hayo.RecipeSerializers.COMPRESSING;
    }

    @Override
    public RecipeType<? extends SingleItemRecipe> getType() {
        return Hayo.RecipeTypes.COMPRESSING;
    }
}
