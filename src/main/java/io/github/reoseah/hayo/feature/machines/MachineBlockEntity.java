package io.github.reoseah.hayo.feature.machines;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.electric_blocks.SimpleElectricBlockEntity;
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
    public static final int MAX_INDUCTION_HEAT = 10_000;

    @Getter
    protected @Nullable RecipeHolder<R> lastRecipe;

    @Getter
    protected int progressEnergy;

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

    protected MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, NonNullList<ItemStack> stacks) {
        super(type, pos, state, stacks);
    }

    public void tickRecipe(ServerLevel level, BlockPos pos, BlockState state) {
        boolean wasProcessing = this.progressEnergy > 0;
        boolean madeProgress = false;

        var input = this.createRecipeInput();
        if (input.isEmpty()) {
            if (this.progressEnergy > 0) {
                this.progressEnergy = 0;
                this.setChanged();
            }
        } else {
            var recipeHolder = this.updateMatchingRecipe(level, input);

            int recipeTotalEnergy = this.getRecipeTotalEnergy(recipeHolder);
            if (this.hasEnoughEnergyToProgress() && this.canCraft(level.registryAccess(), recipeHolder, input)) {
                int usable = Math.min(Math.min(recipeTotalEnergy - this.progressEnergy, this.getEnergyUseRate()), this.storedEnergy);

                this.storedEnergy -= usable;
                this.progressEnergy += usable;
                madeProgress = true;

                if (this.progressEnergy >= recipeTotalEnergy) {
                    this.craft(level.registryAccess(), recipeHolder, input);
                    this.resetRecipeProgress();
                }

                this.setChanged();
            } else if (this.progressEnergy > 0) {
                this.progressEnergy = Mth.clamp(this.progressEnergy - 2 * this.getEnergyUseRate(), 0, recipeTotalEnergy);
                this.setChanged();
            }
        }

        boolean isProcessing = this.progressEnergy > 0;
        if (wasProcessing != isProcessing) {
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, isProcessing));
        }

        if (madeProgress && this.hasInductionUpgrade && this.inductionHeat < MAX_INDUCTION_HEAT) {
            this.inductionHeat += 1;
            this.setChanged();
        } else if (!madeProgress && this.hasInductionUpgrade && this.inductionHeat > 0) {
            this.inductionHeat = Math.max(0, this.inductionHeat - 4);
            this.setChanged();
        }
    }

    protected abstract int getBaseEnergyCapacity();

    protected abstract int getBaseEnergyUseRate();

    protected abstract int getBaseEnergyCost(@Nullable RecipeHolder<R> holder);

    protected abstract boolean isInputSlot(int slot);

    protected abstract int getFirstUpgradeSlot();

    protected abstract int getLastUpgradeSlot();

    protected abstract RecipeType<R> getRecipeType();

    protected abstract I createRecipeInput();

    protected abstract boolean canCraft(RegistryAccess registryAccess, @Nullable RecipeHolder<R> recipe, I recipeInput);

    protected abstract void craft(RegistryAccess registryAccess, RecipeHolder<R> recipe, I input);

    @Override
    public int getEnergyCapacity() {
        return this.getBaseEnergyCapacity() + this.extraCapacity;
    }

    protected boolean hasEnoughEnergyToProgress() {
        return this.storedEnergy >= this.getEnergyUseRate();
    }

    public int getEnergyUseRate() {
        int useRate = (int) (this.getBaseEnergyUseRate() * (1 + this.extraCraftingSpeed));
        if (this.hasInductionUpgrade) {
            return 1 + ((useRate - 1) * this.inductionHeat / MAX_INDUCTION_HEAT);
        }
        return useRate;
    }

    public int getRecipeTotalEnergy(RecipeHolder<R> holder) {
        return (int) (this.getBaseEnergyCost(holder) * (1 + this.extraRecipeCost));
    }

    public int getLastOrDefaultRecipeEnergy() {
        return this.getRecipeTotalEnergy(this.lastRecipe);
    }

    @Override
    protected void inventoryChanged(int slot) {
        super.inventoryChanged(slot);
        if (this.level instanceof ServerLevel) {
            if (this.isInputSlot(slot)) {
                if (this.lastRecipe != null && !this.lastRecipe.value().matches(this.createRecipeInput(), this.level)) {
                    this.resetRecipeProgress();
                }
                return;
            } else if (slot >= this.getFirstUpgradeSlot() && slot <= this.getLastUpgradeSlot()) {
                this.updateUpgradeState();
                return;
            }
        }
    }

    @Override
    @MustBeInvokedByOverriders
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("recipe_used_energy", this.progressEnergy);
        if (this.hasInductionUpgrade) {
            output.putInt("induction_heat", this.inductionHeat);
        }
    }

    @Override
    @MustBeInvokedByOverriders
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.progressEnergy = input.getIntOr("recipe_used_energy", 0);
        this.updateUpgradeState();
        if (this.hasInductionUpgrade) {
            this.inductionHeat = input.getIntOr("induction_heat", 0);
        }
    }

    public @Nullable RecipeHolder<R> updateMatchingRecipe(ServerLevel level, I input) {
        var recipeManager = level.recipeAccess();
        var optional = recipeManager.getRecipeFor(this.getRecipeType(), input, level, this.lastRecipe);
        if (optional.orElse(null) != this.lastRecipe) {
            this.lastRecipe = optional.orElse(null);
        }
        return optional.orElse(null);
    }

    @MustBeInvokedByOverriders
    protected void updateUpgradeState() {
        float extraCraftingSpeed = 0;
        float extraRecipeCost = 0;
        int extraCapacity = 0;
        boolean hasInductionUpgrade = false;

        int firstSlot = this.getFirstUpgradeSlot();
        int lastSlot = this.getLastUpgradeSlot();
        for (int i = firstSlot; i <= lastSlot; i++) {
            var stack = this.stacks.get(i);
            if (stack.is(Hayo.Items.CAPACITOR_UPGRADE)) {
                extraCapacity += 10000;
            }
            if (stack.is(Hayo.Items.OVERCLOCK_UPGRADE)) {
                extraCraftingSpeed += 1;
                extraRecipeCost += 0.25F;
            }
        }

        for (int i = firstSlot; i <= lastSlot; i++) {
            var stack = this.stacks.get(i);
            if (stack.is(Hayo.Items.STREAMLINE_OVERHAUL_UPGRADE)) {
                hasInductionUpgrade = true;
                extraCraftingSpeed += 3;
                break;
            }
        }
        this.extraCapacity = extraCapacity;
        if (this.storedEnergy > this.getEnergyCapacity()) {
            this.storedEnergy = this.getEnergyCapacity();
        }

        this.extraCraftingSpeed = extraCraftingSpeed;

        if (extraRecipeCost != this.extraRecipeCost) {
            this.extraRecipeCost = extraRecipeCost;
            this.resetRecipeProgress();
        }
        if (hasInductionUpgrade != this.hasInductionUpgrade) {
            this.hasInductionUpgrade = hasInductionUpgrade;
            this.inductionHeat = 0;
        }
    }

    protected void resetRecipeProgress() {
        this.progressEnergy = 0;
        this.lastRecipe = null;
    }

    protected static boolean canInsertToSlot(NonNullList<ItemStack> stacks, ItemStack stack, int slot) {
        var currentStack = stacks.get(slot);
        if (currentStack.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(currentStack, stack)) {
            return false;
        }

        return currentStack.getCount() + stack.getCount() <= stack.getMaxStackSize();
    }
}
