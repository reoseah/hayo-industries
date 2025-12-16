package io.github.reoseah.hayoind.block.entity;

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
import org.jetbrains.annotations.Nullable;

public abstract class ProcessingMachineBlockEntity<R extends Recipe<I>, I extends RecipeInput> extends ElectricBlockEntity {
    @Getter
    protected int recipeUsedEnergy;
    @Getter
    protected int recipeTotalEnergy;

    public ProcessingMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("recipe_used_energy", this.recipeUsedEnergy);
        output.putInt("recipe_total_energy", this.recipeTotalEnergy);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.recipeUsedEnergy = input.getIntOr("recipe_used_energy", 0);
        this.recipeTotalEnergy = input.getIntOr("recipe_total_energy", 0);
    }

    protected abstract RecipeManager.CachedCheck<I, R> getRecipeCache();

    public static <R extends Recipe<I>, I extends RecipeInput> void resetRecipe(ServerLevel level, ProcessingMachineBlockEntity<R, I> entity, ProcessingBehavior<R, I> behavior) {
        var recipeInput = behavior.createRecipeInput(entity.stacks);

        var recipeHolder = entity.getRecipeCache().getRecipeFor(recipeInput, level).orElse(null);
        if (recipeHolder != null) {
            entity.recipeTotalEnergy = behavior.getRecipeEnergy(recipeHolder);
            entity.recipeUsedEnergy = 0;
        } else {
            entity.recipeUsedEnergy = entity.recipeTotalEnergy = 0;
        }
    }

    public static <R extends Recipe<I>, I extends RecipeInput> void tickProcessing(ServerLevel level, BlockPos pos, BlockState state, ProcessingMachineBlockEntity<R, I> entity, ProcessingBehavior<R, I> behavior) {
        boolean wasProcessing = entity.recipeUsedEnergy > 0;

        var recipeInput = behavior.createRecipeInput(entity.stacks);
        if (recipeInput.isEmpty()) {
            if (entity.recipeUsedEnergy > 0) {
                entity.recipeUsedEnergy = entity.recipeTotalEnergy = 0;
                entity.setChanged();
            }
        } else {
            var recipeHolder = entity.getRecipeCache().getRecipeFor(recipeInput, level).orElse(null);

            boolean hasEnergy = entity.storedEnergy >= behavior.getEnergyUseRate();
            if (hasEnergy && behavior.canCraft(level.registryAccess(), recipeHolder, recipeInput, entity.stacks)) {
                int usable = Math.min(Math.min(behavior.getEnergyUseRate(), entity.recipeTotalEnergy - entity.recipeUsedEnergy), entity.storedEnergy);

                entity.storedEnergy -= usable;
                entity.recipeUsedEnergy += usable;

                if (entity.recipeUsedEnergy >= entity.recipeTotalEnergy) {
                    behavior.craft(level.registryAccess(), recipeHolder, recipeInput, entity.stacks);
                    resetRecipe(level, entity, behavior);
                }

                entity.setChanged();
            } else {
                entity.recipeUsedEnergy = Mth.clamp(entity.recipeUsedEnergy - 2 * behavior.getEnergyUseRate(), 0, entity.recipeTotalEnergy);
                entity.setChanged();
            }
        }

        boolean isProcessing = entity.recipeUsedEnergy > 0;
        if (wasProcessing != isProcessing) {
            level.setBlockAndUpdate(pos, state.setValue(OrientableMachineBlock.LIT, isProcessing));
        }
    }

    public interface ProcessingBehavior<R extends Recipe<I>, I extends RecipeInput> {
        I createRecipeInput(NonNullList<ItemStack> items);

        int getEnergyUseRate();

        int getRecipeEnergy(RecipeHolder<R> recipe);

        boolean canCraft(
                RegistryAccess registryAccess,
                @Nullable RecipeHolder<R> recipe,
                I recipeInput,
                NonNullList<ItemStack> items
        );

        void craft(
                RegistryAccess registryAccess,
                RecipeHolder<R> recipe,
                I recipeInput,
                NonNullList<ItemStack> items
        );
    }

    public interface SingleProcessingBehavior<R extends Recipe<SingleRecipeInput>> extends ProcessingBehavior<R, SingleRecipeInput> {
        int INPUT_SLOT = 0;
        int BATTERY_SLOT = 1;
        int OUTPUT_SLOT = 2;

        @Override
        default SingleRecipeInput createRecipeInput(NonNullList<ItemStack> items) {
            return new SingleRecipeInput(items.get(INPUT_SLOT));
        }

        @Override
        default boolean canCraft(RegistryAccess registryAccess, @Nullable RecipeHolder<R> recipe, SingleRecipeInput recipeInput, NonNullList<ItemStack> items) {
            if (recipe == null || recipeInput.isEmpty()) {
                return false;
            }

            var recipeOutput = recipe.value().assemble(recipeInput, registryAccess);
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
        default void craft(RegistryAccess registryAccess, RecipeHolder<R> recipe, SingleRecipeInput recipeInput, NonNullList<ItemStack> items) {
            var inputStack = items.get(INPUT_SLOT);
            var outputStack = items.get(OUTPUT_SLOT);
            var recipeOutput = recipe.value().assemble(recipeInput, registryAccess);

            if (outputStack.isEmpty()) {
                items.set(OUTPUT_SLOT, recipeOutput);
            } else {
                outputStack.grow(recipeOutput.getCount());
            }

            inputStack.shrink(1);
        }
    }
}
