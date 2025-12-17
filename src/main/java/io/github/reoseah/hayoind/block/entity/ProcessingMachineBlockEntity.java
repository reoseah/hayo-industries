package io.github.reoseah.hayoind.block.entity;

import io.github.reoseah.hayoind.Hayo;
import io.github.reoseah.hayoind.block.OrientableMachineBlock;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

public abstract class ProcessingMachineBlockEntity<R extends Recipe<I>, I extends RecipeInput> extends ElectricBlockEntity {
    @Getter
    protected int recipeUsedEnergy;
    @Getter
    protected int recipeTotalEnergy;

    public ProcessingMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract RecipeManager.CachedCheck<I, R> getRecipeCache();

    protected abstract int getRecipeEnergy(RecipeHolder<R> holder);

    protected abstract int getEnergyUseRate();

    public abstract int getEnergyCapacity();

    @Override
    @MustBeInvokedByOverriders
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("recipe_used_energy", this.recipeUsedEnergy);
        output.putInt("recipe_total_energy", this.recipeTotalEnergy);
    }

    @Override
    @MustBeInvokedByOverriders
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.recipeUsedEnergy = input.getIntOr("recipe_used_energy", 0);
        this.recipeTotalEnergy = input.getIntOr("recipe_total_energy", 0);
    }

    public static <R extends Recipe<I>, I extends RecipeInput> void resetProcessing(ServerLevel level, ProcessingMachineBlockEntity<R, I> entity, RecipeSlotsBehavior<R, I> behavior) {
        var input = behavior.createRecipeInput(entity.stacks);
        var recipeHolder = entity.getRecipeCache().getRecipeFor(input, level).orElse(null);
        if (recipeHolder != null) {
            entity.recipeTotalEnergy = entity.getRecipeEnergy(recipeHolder);
            entity.recipeUsedEnergy = 0;
        } else {
            entity.recipeUsedEnergy = entity.recipeTotalEnergy = 0;
        }
    }

    public static <R extends Recipe<I>, I extends RecipeInput> void tickProcessing(ServerLevel level, BlockPos pos, BlockState state, ProcessingMachineBlockEntity<R, I> entity, RecipeSlotsBehavior<R, I> behavior) {
        boolean wasProcessing = entity.recipeUsedEnergy > 0;

        var input = behavior.createRecipeInput(entity.stacks);
        if (input.isEmpty()) {
            if (entity.recipeUsedEnergy > 0) {
                entity.recipeUsedEnergy = entity.recipeTotalEnergy = 0;
                entity.setChanged();
            }
        } else {
            var recipeHolder = entity.getRecipeCache().getRecipeFor(input, level).orElse(null);

            boolean hasEnergy = entity.storedEnergy >= entity.getEnergyUseRate();
            if (hasEnergy && behavior.canCraft(level.registryAccess(), recipeHolder, input, entity.stacks)) {
                int usable = Math.min(Math.min(entity.getEnergyUseRate(), entity.recipeTotalEnergy - entity.recipeUsedEnergy), entity.storedEnergy);

                entity.storedEnergy -= usable;
                entity.recipeUsedEnergy += usable;

                if (entity.recipeUsedEnergy >= entity.recipeTotalEnergy) {
                    behavior.craft(level.registryAccess(), recipeHolder, input, entity.stacks);
                    resetProcessing(level, entity, behavior);
                }

                entity.setChanged();
            } else {
                entity.recipeUsedEnergy = Mth.clamp(entity.recipeUsedEnergy - 2 * entity.getEnergyUseRate(), 0, entity.recipeTotalEnergy);
                entity.setChanged();
            }
        }

        boolean isProcessing = entity.recipeUsedEnergy > 0;
        if (wasProcessing != isProcessing) {
            level.setBlockAndUpdate(pos, state.setValue(OrientableMachineBlock.LIT, isProcessing));
        }
    }

    protected static int getCapacityFromUpgrades(NonNullList<ItemStack> stacks, int firstUpgradeSlot, int upgradeSlots) {
        int capacity = 0;
        for (int i = firstUpgradeSlot; i < firstUpgradeSlot + upgradeSlots; i++) {
            var stack = stacks.get(i);
            if (stack.is(Hayo.Items.CAPACITOR_UPGRADE)) {
                capacity += 10000;
            }
        }
        return capacity;
    }

    public interface RecipeSlotsBehavior<R extends Recipe<I>, I extends RecipeInput> {
        I createRecipeInput(NonNullList<ItemStack> items);

        boolean canCraft(RegistryAccess registryAccess, @Nullable RecipeHolder<R> recipe, I recipeInput, NonNullList<ItemStack> items);

        void craft(RegistryAccess registryAccess, RecipeHolder<R> recipe, I recipeInput, NonNullList<ItemStack> items);

        @SuppressWarnings("unchecked")
        static <R extends Recipe<SingleRecipeInput>> RecipeSlotsBehavior<R, SingleRecipeInput> oneInputOneOutput() {
            return (RecipeSlotsBehavior<R, SingleRecipeInput>) OneInputOneOutput.INSTANCE;
        }

        enum OneInputOneOutput implements RecipeSlotsBehavior<Recipe<SingleRecipeInput>, SingleRecipeInput> {
            INSTANCE;

            public static final int INPUT_SLOT = 0;
            public static final int OUTPUT_SLOT = 2;

            @Override
            public SingleRecipeInput createRecipeInput(NonNullList<ItemStack> items) {
                return new SingleRecipeInput(items.get(INPUT_SLOT));
            }

            @Override
            public boolean canCraft(RegistryAccess registryAccess, @Nullable RecipeHolder<Recipe<SingleRecipeInput>> recipe, SingleRecipeInput input, NonNullList<ItemStack> items) {
                if (recipe == null || input.isEmpty()) {
                    return false;
                }

                var recipeOutput = recipe.value().assemble(input, registryAccess);
                if (recipeOutput.isEmpty()) {
                    return false;
                }

                var outputStack = items.get(OUTPUT_SLOT);
                if (outputStack.isEmpty()) {
                    return true;
                }
                if (!ItemStack.isSameItemSameComponents(outputStack, recipeOutput)) {
                    return false;
                }

                return outputStack.getCount() + recipeOutput.getCount() <= recipeOutput.getMaxStackSize();
            }

            @Override
            public void craft(RegistryAccess registryAccess, RecipeHolder<Recipe<SingleRecipeInput>> recipe, SingleRecipeInput input, NonNullList<ItemStack> items) {
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
        }
    }
}
