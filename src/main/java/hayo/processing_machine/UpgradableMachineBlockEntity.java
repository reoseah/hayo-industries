package hayo.processing_machine;

import hayo.Hayo;
import hayo.common.IntRange;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

public abstract class UpgradableMachineBlockEntity<R extends Recipe<I>, I extends RecipeInput> extends MachineBlockEntity<R, I> {
    public static final int MAX_INDUCTION_HEAT = 10_000;

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

    protected abstract IntRange getUpgradeSlots();

    @Override
    public int getEnergyCapacity() {
        return this.getBaseEnergyCapacity() + this.extraCapacity;
    }

    protected abstract int getBaseEnergyCapacity();

    @Override
    public final int getEnergyUseRate() {
        return (int) (this.getBaseEnergyUseRate() * (1 + this.extraCraftingSpeed));
    }

    @Override
    protected int getAmountToProgressRecipe(int usableEnergy) {
        if (this.hasInductionUpgrade) {
            return 1 + (usableEnergy - 1) * this.inductionHeat / MAX_INDUCTION_HEAT;
        }
        return super.getAmountToProgressRecipe(usableEnergy);
    }

    protected abstract int getBaseEnergyUseRate();

    @Override
    public final int getEnergyCost(@Nullable RecipeHolder<R> holder) {
        return (int) (this.getBaseEnergyCost(holder) * (1 + this.extraRecipeCost));
    }

    protected abstract int getBaseEnergyCost(@Nullable RecipeHolder<R> holder);

    @Override
    public boolean tickRecipe(ServerLevel level, BlockPos pos, BlockState state) {
        boolean madeProgress = super.tickRecipe(level, pos, state);
        if (madeProgress && this.hasInductionUpgrade && this.inductionHeat < MAX_INDUCTION_HEAT) {
            this.inductionHeat += 1;
            this.setChanged();
        } else if (!madeProgress && this.hasInductionUpgrade && this.inductionHeat > 0) {
            this.inductionHeat = Math.max(0, this.inductionHeat - 4);
            this.setChanged();
        }
        return madeProgress;
    }

    @Override
    @MustBeInvokedByOverriders
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.hasInductionUpgrade) {
            output.putInt("induction_heat", this.inductionHeat);
        }
    }

    @Override
    @MustBeInvokedByOverriders
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.updateUpgradeState();
        if (this.hasInductionUpgrade) {
            this.inductionHeat = input.getIntOr("induction_heat", 0);
        }
    }

    @Override
    protected void inventoryChanged(int slot, ItemStack previous, ItemStack stack) {
        super.inventoryChanged(slot, previous, stack);
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
            this.recipeProgress = 0;
        }
        if (hasInductionUpgrade != this.hasInductionUpgrade) {
            this.hasInductionUpgrade = hasInductionUpgrade;
            this.inductionHeat = 0;
        }
    }
}
