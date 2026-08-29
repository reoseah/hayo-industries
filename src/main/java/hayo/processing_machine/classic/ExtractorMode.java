package hayo.processing_machine.classic;

import hayo.Hayo;
import net.minecraft.world.item.crafting.RecipeType;

public enum ExtractorMode {
    DEFAULT(Hayo.RecipeTypes.EXTRACTING),
    NUTRIENT_EXTRACTING(Hayo.RecipeTypes.NUTRIENT_EXTRACTING);

    public final RecipeType<? extends ClassicMachineRecipe> recipeType;

    ExtractorMode(RecipeType<? extends ClassicMachineRecipe> recipeType) {
        this.recipeType = recipeType;
    }
}
