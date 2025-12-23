package io.github.reoseah.hayo.feature.processing_machines;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class SimpleMachineBlockEntity<R extends Recipe<SingleRecipeInput>> extends MachineBlockEntity<R, SingleRecipeInput> {
    public static final int SLOTS = 7;
    public static final int INPUT_SLOT = 0;
    public static final int BATTERY_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int FIRST_UPGRADE_SLOT = 3;
    public static final int LAST_UPGRADE_SLOT = 6;

    public SimpleMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public int getSlotCount() {
        return SLOTS;
    }

    @Override
    public boolean isInputSlot(int slot) {
        return slot == INPUT_SLOT;
    }

    @Override
    public SingleRecipeInput createRecipeInput(NonNullList<ItemStack> items) {
        return new SingleRecipeInput(items.get(INPUT_SLOT));
    }

    @Override
    public boolean canCraft(RegistryAccess registryAccess, @Nullable RecipeHolder<R> recipe, SingleRecipeInput input, NonNullList<ItemStack> items) {
        if (recipe == null || input.isEmpty()) {
            return false;
        }

        var recipeOutput = recipe.value().assemble(input, registryAccess);
        return canInsertToSlot(items, recipeOutput, OUTPUT_SLOT);
    }

    @Override
    public void craft(RegistryAccess registryAccess, RecipeHolder<R> recipe, SingleRecipeInput input, NonNullList<ItemStack> items) {
        var recipeOutput = recipe.value().assemble(input, registryAccess);
        var outputStack = items.get(OUTPUT_SLOT);

        if (outputStack.isEmpty()) {
            items.set(OUTPUT_SLOT, recipeOutput);
        } else {
            outputStack.grow(recipeOutput.getCount());
        }

        var inputStack = items.get(INPUT_SLOT);
        inputStack.shrink(1);
    }

    @Override
    public int getFirstUpgradeSlot() {
        return FIRST_UPGRADE_SLOT;
    }

    @Override
    public int getLastUpgradeSlot() {
        return LAST_UPGRADE_SLOT;
    }
}
