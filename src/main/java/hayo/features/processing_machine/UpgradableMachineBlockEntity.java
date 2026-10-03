package hayo.features.processing_machine;

import hayo.Hayo;
import hayo.common.IntRange;
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
import org.jspecify.annotations.Nullable;

public abstract class UpgradableMachineBlockEntity<R extends Recipe<I>, I extends RecipeInput> extends EnergyReceiverBlockEntity implements RecipeState.Context<R, I> {
    public static final int MAX_INDUCTION_HEAT = 10_000;

    public RecipeState<R, I> recipeState = new RecipeState<>();

    @Getter
    protected float extraCraftingSpeed = 0;
    @Getter
    protected float extraRecipeCost = 0;
    @Getter
    protected int extraCapacity = 0;
    @Getter
    protected boolean hasInductionUpgrade = false;
    @Getter
    protected int inductionHeat = 0;

    protected UpgradableMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, NonNullList<ItemStack> stacks) {
        super(type, pos, state, stacks);
    }

    @Override
    public int getEnergyCapacity() {
        return this.getBaseEnergyCapacity() + this.extraCapacity;
    }

    protected abstract int getBaseEnergyCapacity();

    public final int getEnergyUseRate() {
        return (int) (this.getBaseEnergyUseRate() * (1 + this.extraCraftingSpeed));
    }

    protected abstract int getBaseEnergyUseRate();

    protected abstract RecipeType<R> getRecipeType();

    protected abstract boolean isInputSlot(int slot);

    protected abstract I createRecipeInput();

    @Override
    public final int getRecipeCost(@Nullable R holder) {
        return (int) (this.getBaseEnergyCost(holder) * (1 + this.extraRecipeCost));
    }

    protected abstract int getBaseEnergyCost(@Nullable R recipe);


    protected abstract IntRange getUpgradeSlots();

    public void tickRecipe(ServerLevel level, BlockPos pos, BlockState state) {
        boolean wasProcessing = this.recipeState.progress > 0;

        int progress = this.recipeState.tick(this.getRecipeType(), this.createRecipeInput(), (ServerLevel) level, this);
        if (progress > 0) {
            this.storedEnergy -= this.getEnergyUseRate();
        }

        if (progress > 0 && this.hasInductionUpgrade && this.inductionHeat < MAX_INDUCTION_HEAT) {
            this.inductionHeat += 1;
        } else if (progress <= 0 && this.hasInductionUpgrade && this.inductionHeat > 0) {
            this.inductionHeat = Math.max(0, this.inductionHeat - 4);
            this.setChanged();
        }

        boolean isProcessing = this.recipeState.progress > 0;
        if (wasProcessing != isProcessing) {
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, isProcessing));
        }
    }

    @Override
    public RecipeState.EnergyState getEnergyState() {
        int energyUseRate = this.getEnergyUseRate();
        if (energyUseRate <= this.storedEnergy) {
            return new RecipeState.EnergyState.Sufficient(
                    this.hasInductionUpgrade ? 1 + (energyUseRate - 1) * this.inductionHeat / MAX_INDUCTION_HEAT : energyUseRate
            );
        } else {
            return new RecipeState.EnergyState.NotSufficient(-2 * this.getEnergyUseRate());
        }
    }

    public boolean hasRecipe() {
        return this.recipeState.lastMatch != null && this.recipeState.lastMatch.value().matches(this.createRecipeInput(), this.level);
    }

    public int getLastOrDefaultRecipeCost() {
        return this.getRecipeCost(this.recipeState.lastMatch != null ? this.recipeState.lastMatch.value() : null);
    }

    @Override
    @MustBeInvokedByOverriders
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("recipe_progress", this.recipeState.progress);

        if (this.hasInductionUpgrade) {
            output.putInt("induction_heat", this.inductionHeat);
        }
    }

    @Override
    @MustBeInvokedByOverriders
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.recipeState.progress = input.getIntOr("recipe_progress", 0);

        this.updateUpgradeState();
        if (this.hasInductionUpgrade) {
            this.inductionHeat = input.getIntOr("induction_heat", 0);
        }
    }

    @Override
    protected void inventoryChanged(int slot, ItemStack previous, ItemStack stack) {
        super.inventoryChanged(slot, previous, stack);
        if (this.isInputSlot(slot)) {
            if (!stack.isEmpty() && !ItemStack.isSameItemSameComponents(stack, previous)) {
                this.recipeState.progress = 0;
            }
        }
        if (this.getUpgradeSlots().contains(slot)) {
            this.updateUpgradeState();
        }
    }

    @MustBeInvokedByOverriders
    protected void updateUpgradeState() {
        float extraCraftingSpeed = 0;
        float extraRecipeCost = 0;
        int extraCapacity = 0;
        boolean hasInductionUpgrade = false;

        var upgradeSlots = this.getUpgradeSlots();
        for (int i = upgradeSlots.start(); i < upgradeSlots.end(); i++) {
            var stack = this.stacks.get(i);
            if (stack.is(Hayo.Items.CAPACITOR_UPGRADE)) {
                extraCapacity += 10000;
            }
            if (stack.is(Hayo.Items.OVERCLOCK_UPGRADE)) {
                extraCraftingSpeed += 1;
                extraRecipeCost += 0.25F;
            }
            if (stack.is(Hayo.Items.STREAMLINE_OVERHAUL_UPGRADE) && !hasInductionUpgrade) {
                hasInductionUpgrade = true;
                extraCraftingSpeed += 3;
            }
        }

        this.extraCapacity = extraCapacity;
        if (this.storedEnergy > this.getEnergyCapacity()) {
            this.storedEnergy = this.getEnergyCapacity();
        }

        this.extraCraftingSpeed = extraCraftingSpeed;

        if (extraRecipeCost != this.extraRecipeCost) {
            this.extraRecipeCost = extraRecipeCost;
            this.recipeState.progress = 0;
        }
        if (hasInductionUpgrade != this.hasInductionUpgrade) {
            this.hasInductionUpgrade = hasInductionUpgrade;
            this.inductionHeat = 0;
        }
    }
}
