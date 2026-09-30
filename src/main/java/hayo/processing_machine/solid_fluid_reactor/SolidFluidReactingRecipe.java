package hayo.processing_machine.solid_fluid_reactor;

import hayo.fluid_stack.FluidIngredientAmount;
import hayo.fluid_stack.FluidStack;
import hayo.fluid_stack.ItemFluidRecipeInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface SolidFluidReactingRecipe extends Recipe<ItemFluidRecipeInput> {
    Ingredient inputItem();

    FluidIngredientAmount inputFluid();

    List<ItemStackTemplate> resultItems();

    FluidStack resultFluid();

    int energyCost();

    @Override
    default ItemStack assemble(ItemFluidRecipeInput input) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean showNotification() {
        return false;
    }

    @Override
    default String group() {
        return "";
    }

    @Override
    default PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    default @Nullable RecipeBookCategory recipeBookCategory() {
        return null;
    }

    @Override
    default boolean isSpecial() {
        return true;
    }
}
