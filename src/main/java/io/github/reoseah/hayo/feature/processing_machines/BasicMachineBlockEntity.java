package io.github.reoseah.hayo.feature.processing_machines;

import io.github.reoseah.hayo.api.energy.ElectricItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class BasicMachineBlockEntity<R extends Recipe<SingleRecipeInput>> extends MachineBlockEntity<R, SingleRecipeInput> implements WorldlyContainer {
    public static final int SLOTS = 7;
    public static final int INPUT_SLOT = 0;
    public static final int BATTERY_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int FIRST_UPGRADE_SLOT = 3;
    public static final int LAST_UPGRADE_SLOT = 6;

    public BasicMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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
    public int getFirstUpgradeSlot() {
        return FIRST_UPGRADE_SLOT;
    }

    @Override
    public int getLastUpgradeSlot() {
        return LAST_UPGRADE_SLOT;
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
        if (recipe.value() instanceof BasicMachineRecipe machineRecipe && machineRecipe.extraChance > 0) {
            recipeOutput.setCount(recipeOutput.getCount() + 1);
        }
        return canInsertToSlot(items, recipeOutput, OUTPUT_SLOT);
    }

    @Override
    public void craft(RegistryAccess registryAccess, RecipeHolder<R> recipe, SingleRecipeInput input, NonNullList<ItemStack> items) {
        var inputStack = items.get(INPUT_SLOT);
        inputStack.shrink(1);

        var recipeOutput = recipe.value().assemble(input, registryAccess);
        if (recipe.value() instanceof BasicMachineRecipe machineRecipe && machineRecipe.extraChance > 0) {
            if (this.level.getRandom().nextFloat() < machineRecipe.extraChance) {
                recipeOutput.setCount(recipeOutput.getCount() + 1);
            }
        }

        var outputStack = items.get(OUTPUT_SLOT);
        if (outputStack.isEmpty()) {
            items.set(OUTPUT_SLOT, recipeOutput);
        } else {
            outputStack.grow(recipeOutput.getCount());
        }
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return switch (side) {
            case UP -> new int[]{INPUT_SLOT};
            case DOWN -> new int[]{OUTPUT_SLOT};
            default -> new int[]{BATTERY_SLOT};
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction side) {
        return switch (side) {
            case null -> index != OUTPUT_SLOT;
            case UP -> true;
            case DOWN -> false;
            default -> ElectricItems.isElectric(stack);
        };
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction side) {
        return side != Direction.UP;
    }
}
