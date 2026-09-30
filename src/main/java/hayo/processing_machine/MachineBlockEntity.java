package hayo.processing_machine;

import hayo.common.blockentity.EnergyReceiverBlockEntity;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class MachineBlockEntity<R extends Recipe<I>, I extends RecipeInput> extends EnergyReceiverBlockEntity implements RecipeState.Context<R, I> {
    @Getter
    protected RecipeState<R, I> recipeState = new RecipeState<>();

    protected MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, NonNullList<ItemStack> stacks) {
        super(type, pos, state, stacks);
    }

    public boolean tickRecipe(ServerLevel level, BlockPos pos, BlockState state) {
        boolean wasProcessing = this.recipeState.progress > 0;

        int energyUsed = this.recipeState.tick(
                this.getRecipeType(),
                this.createRecipeInput(),
                (ServerLevel) level,
                this.hasEnoughEnergyToProgress()
                        ? new RecipeState.RecipeResourceState.Sufficient(this.getEnergyUseRate())
                        : new RecipeState.RecipeResourceState.NotSufficient(-2 * this.getEnergyUseRate()),
                this
        );
        this.storedEnergy -= energyUsed;

        boolean isProcessing = this.recipeState.progress > 0;
        if (wasProcessing != isProcessing) {
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, isProcessing));
        }

        return energyUsed > 0;
    }

    public abstract int getEnergyUseRate();

    public int getLastOrDefaultRecipeCost() {
        return this.getRecipeCost(this.recipeState.lastMatch != null ? this.recipeState.lastMatch.value() : null);
    }

    protected boolean hasEnoughEnergyToProgress() {
        return this.storedEnergy >= this.getEnergyUseRate();
    }

    protected int getAmountToProgressRecipe(int usableEnergy) {
        return usableEnergy;
    }

    protected abstract RecipeType<R> getRecipeType();

    protected abstract boolean isInputSlot(int slot);

    protected abstract I createRecipeInput();

    @Override
    protected void inventoryChanged(int slot, ItemStack previous, ItemStack stack) {
        super.inventoryChanged(slot, previous, stack);
        if (this.isInputSlot(slot)) {
            if (!stack.isEmpty() && !ItemStack.isSameItemSameComponents(stack, previous)) {
                this.recipeState.progress = 0;
            }
        }
    }

    @Override
    @MustBeInvokedByOverriders
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("recipe_progress", this.recipeState.progress);
    }

    @Override
    @MustBeInvokedByOverriders
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.recipeState.progress = input.getIntOr("recipe_progress", 0);
    }

    public boolean hasRecipe() {
        return this.recipeState.lastMatch != null && this.recipeState.lastMatch.value().matches(this.createRecipeInput(), this.level);
    }
}
