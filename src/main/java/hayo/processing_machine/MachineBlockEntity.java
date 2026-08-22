package hayo.processing_machine;

import hayo.common.blockentity.SimpleElectricBlockEntity;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

public abstract class MachineBlockEntity<R extends Recipe<I>, I extends RecipeInput> extends SimpleElectricBlockEntity {
    @Getter
    protected @Nullable RecipeHolder<R> lastRecipe;

    @Getter
    protected int recipeProgress;

    protected MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, NonNullList<ItemStack> stacks) {
        super(type, pos, state, stacks);
    }

    public boolean tickRecipe(ServerLevel level, BlockPos pos, BlockState state) {
        boolean wasProcessing = this.recipeProgress > 0;
        boolean madeProgress = false;

        var input = this.createRecipeInput();
        if (input.isEmpty()) {
            if (this.recipeProgress > 0) {
                this.recipeProgress = 0;
                this.setChanged();
            }
        } else {
            var recipeHolder = this.findMatchingRecipe(level, input);

            int recipeCost = this.getEnergyCost(recipeHolder);
            if (this.hasEnoughEnergyToProgress() && this.canCraft(level.registryAccess(), recipeHolder, input)) {
                int usableEnergy = Math.min(Math.min(recipeCost - this.recipeProgress, this.getEnergyUseRate()), this.storedEnergy);

                this.storedEnergy -= usableEnergy;
                this.recipeProgress += this.getAmountToProgressRecipe(usableEnergy);
                madeProgress = true;

                if (this.recipeProgress >= recipeCost) {
                    this.craft(level.registryAccess(), recipeHolder, input);
                    this.recipeProgress = 0;
                }

                this.setChanged();
            } else if (this.recipeProgress > 0) {
                this.recipeProgress = Mth.clamp(this.recipeProgress - 2 * this.getEnergyUseRate(), 0, recipeCost);
                this.setChanged();
            }
        }

        boolean isProcessing = this.recipeProgress > 0;
        if (wasProcessing != isProcessing) {
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, isProcessing));
        }

        return madeProgress;
    }


    public abstract int getEnergyUseRate();

    public int getLastOrDefaultRecipeCost() {
        return this.getEnergyCost(this.lastRecipe);
    }

    public abstract int getEnergyCost(@Nullable RecipeHolder<R> holder);

    protected boolean hasEnoughEnergyToProgress() {
        return this.storedEnergy >= this.getEnergyUseRate();
    }

    protected int getAmountToProgressRecipe(int usableEnergy) {
        return usableEnergy;
    }

    protected abstract RecipeType<R> getRecipeType();

    protected abstract boolean isInputSlot(int slot);

    protected abstract I createRecipeInput();

    protected abstract boolean canCraft(RegistryAccess registryAccess, @Nullable RecipeHolder<R> recipe, I recipeInput);

    protected abstract void craft(RegistryAccess registryAccess, RecipeHolder<R> recipe, I input);

    @Override
    protected void inventoryChanged(int slot, ItemStack previous, ItemStack stack) {
        super.inventoryChanged(slot, previous, stack);
        if (this.isInputSlot(slot)) {
            if (!stack.isEmpty() && !ItemStack.isSameItemSameComponents(stack, previous)) {
                this.recipeProgress = 0;
            }
        }
    }

    @Override
    @MustBeInvokedByOverriders
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("recipe_progress", this.recipeProgress);
    }

    @Override
    @MustBeInvokedByOverriders
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.recipeProgress = input.getIntOr("recipe_progress", 0);
    }

    public @Nullable RecipeHolder<R> findMatchingRecipe(ServerLevel level, I input) {
        var recipeManager = level.recipeAccess();
        var match = recipeManager.getRecipeFor(this.getRecipeType(), input, level, this.lastRecipe).orElse(null);
        if (match != this.lastRecipe) {
            this.lastRecipe = match;
        }
        return match;
    }

    protected boolean canInsertToSlot(ItemStack stack, int slot) {
        var currentStack = this.stacks.get(slot);
        return currentStack.isEmpty()
                || ItemStack.isSameItemSameComponents(currentStack, stack)
                && currentStack.getCount() + stack.getCount() <= Math.min(stack.getMaxStackSize(), this.getMaxStackSize(stack));
    }
}
