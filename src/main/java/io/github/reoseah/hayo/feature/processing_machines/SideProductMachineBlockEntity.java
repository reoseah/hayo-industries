package io.github.reoseah.hayo.feature.processing_machines;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class SideProductMachineBlockEntity<R extends MachineRecipeWithSideProduct> extends MachineBlockEntity<R, SingleRecipeInput> {
    public static final int SLOTS = 8;
    public static final int INPUT_SLOT = 0;
    public static final int BATTERY_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int SECOND_OUTPUT_SLOT = 2;
    public static final int FIRST_UPGRADE_SLOT = 4;
    public static final int LAST_UPGRADE_SLOT = 7;

    public SideProductMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected int getSlotCount() {
        return SLOTS;
    }

    @Override
    protected boolean isInputSlot(int slot) {
        return slot == INPUT_SLOT;
    }

    @Override
    protected int getFirstUpgradeSlot() {
        return FIRST_UPGRADE_SLOT;
    }

    @Override
    protected int getLastUpgradeSlot() {
        return LAST_UPGRADE_SLOT;
    }

    @Override
    protected SingleRecipeInput createRecipeInput(NonNullList<ItemStack> items) {
        return new SingleRecipeInput(items.get(INPUT_SLOT));
    }

    @Override
    protected boolean canCraft(RegistryAccess registryAccess, @Nullable RecipeHolder<R> recipe, SingleRecipeInput input, NonNullList<ItemStack> items) {
        if (recipe == null || input.isEmpty()) {
            return false;
        }

        var recipeOutput = recipe.value().result();
        if (!canInsertToSlot(items, recipeOutput, OUTPUT_SLOT)) {
            return false;
        }

        var recipeSecondary = recipe.value().getSecondaryResult();
        if (recipeSecondary.isEmpty()) {
            return true;
        }
        return canInsertToSlot(items, recipeSecondary, SECOND_OUTPUT_SLOT);
    }

    @Override
    protected void craft(RegistryAccess registryAccess, RecipeHolder<R> holder, SingleRecipeInput input, NonNullList<ItemStack> items) {
        var recipe = holder.value();

        var recipeOutput = recipe.result().copy();
        var outputStack = items.get(OUTPUT_SLOT);
        if (outputStack.isEmpty()) {
            items.set(OUTPUT_SLOT, recipeOutput);
        } else {
            outputStack.grow(recipeOutput.getCount());
        }

        if (recipe.getSecondaryResultChance() != 0 && this.level.getRandom().nextFloat() < recipe.getSecondaryResultChance()) {
            var secondRecipeOutput = recipe.getSecondaryResult().copy();
            var secondStack = items.get(SECOND_OUTPUT_SLOT);
            if (secondStack.isEmpty()) {
                items.set(SECOND_OUTPUT_SLOT, secondRecipeOutput);
            } else {
                secondStack.grow(secondRecipeOutput.getCount());
            }
        }

        var inputStack = items.get(INPUT_SLOT);
        inputStack.shrink(1);
    }
}