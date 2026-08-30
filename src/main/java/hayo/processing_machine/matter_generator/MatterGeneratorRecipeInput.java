package hayo.processing_machine.matter_generator;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

public record MatterGeneratorRecipeInput(List<ItemStack> upgrades) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return this.upgrades.get(index);
    }

    @Override
    public int size() {
        return this.upgrades.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
