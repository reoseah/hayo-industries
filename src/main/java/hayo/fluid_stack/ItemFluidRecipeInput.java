package hayo.fluid_stack;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record ItemFluidRecipeInput(ItemStack item, FluidStack fluid) implements RecipeInput {
    @Override
    public int size() {
        return 1;
    }

    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? this.item : null;
    }
}
