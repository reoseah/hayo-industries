package hayo.feature.machines.electric_furnace;

import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;

public enum ElectricFurnaceMode {
    SMELTING(RecipeType.SMELTING), BLASTING(RecipeType.BLASTING), SMOKING(RecipeType.SMOKING);

    public final RecipeType<? extends AbstractCookingRecipe> recipeType;

    ElectricFurnaceMode(RecipeType<? extends AbstractCookingRecipe> recipeType) {
        this.recipeType = recipeType;
    }
}
