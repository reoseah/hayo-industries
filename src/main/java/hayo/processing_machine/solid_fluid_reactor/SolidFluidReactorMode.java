package hayo.processing_machine.solid_fluid_reactor;

import hayo.Hayo;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;

public enum SolidFluidReactorMode {
    REACTING(Hayo.RecipeTypes.SOLID_FLUID_REACTING),
    ORE_WASHING(Hayo.RecipeTypes.ORE_WASHING),
    NUTRIENT_PURIFYING(Hayo.RecipeTypes.NUTRIENT_PURIFYING);

    public final RecipeType<? extends BaseSolidFluidReactingRecipe> recipeType;

    SolidFluidReactorMode(RecipeType<? extends BaseSolidFluidReactingRecipe> recipeType) {
        this.recipeType = recipeType;
    }
}
