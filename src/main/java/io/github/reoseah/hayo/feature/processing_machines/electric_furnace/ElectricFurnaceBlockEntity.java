package io.github.reoseah.hayo.feature.processing_machines.electric_furnace;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.processing_machines.ClassicMachineBlockEntity;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class ElectricFurnaceBlockEntity extends ClassicMachineBlockEntity<AbstractCookingRecipe> {
    public static final int TRANSFER_LIMIT = 32;
    public static final int ENERGY_USE_RATE = 3;
    public static final int CAPACITY = energyCostFromCookingTime(AbstractFurnaceBlockEntity.BURN_TIME_STANDARD); /* 200 * 3/4 * 3 = 450 e */
    public static final int MAX_INDUCTION_HEAT = 10_000;

    @Getter
    protected ElectricFurnaceMode mode = ElectricFurnaceMode.NORMAL;

    @Getter
    protected boolean hasInductionUpgrade = false;
    @Getter
    protected int inductionHeat = 0;

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.ELECTRIC_FURNACE, pos, state);
    }

    public static int energyCostFromCookingTime(AbstractCookingRecipe recipe) {
        int cookingTime = recipe.cookingTime();
        var type = recipe.getType();
        if (type == RecipeType.BLASTING || type == RecipeType.SMOKING) {
            return energyCostFromCookingTime(2 * cookingTime);
        }
        return energyCostFromCookingTime(cookingTime);
    }

    public static int energyCostFromCookingTime(int cookingTime) {
        return cookingTime * ENERGY_USE_RATE * 3 / 4;
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, ElectricFurnaceBlockEntity entity) {
        entity.chargeFromSlot(BATTERY_SLOT);
        boolean madeProgress = tickProcessing((ServerLevel) level, pos, state, entity);
        if (madeProgress && entity.hasInductionUpgrade && entity.inductionHeat < MAX_INDUCTION_HEAT) {
            entity.inductionHeat += 1;
        } else if (!madeProgress && entity.hasInductionUpgrade && entity.inductionHeat > 0) {
            entity.inductionHeat = Math.max(0, entity.inductionHeat - 4);
        }
        entity.resetEnergyPerTick();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected RecipeType<AbstractCookingRecipe> getRecipeType() {
        return (RecipeType<AbstractCookingRecipe>) this.mode.recipeType;
    }

    @Override
    public int getDefaultEnergyUseRate() {
        return ENERGY_USE_RATE;
    }

    @Override
    public int getDefaultCapacity() {
        return CAPACITY;
    }

    @Override
    protected int getEnergyTransferLimit() {
        return TRANSFER_LIMIT;
    }

    @Override
    public int getDefaultEnergyCost(RecipeHolder<AbstractCookingRecipe> recipe) {
        return energyCostFromCookingTime(recipe.value());
    }

    @Override
    public int getEnergyUseRate() {
        var useRate = super.getEnergyUseRate();

        if (this.mode != ElectricFurnaceMode.NORMAL) {
            useRate += ENERGY_USE_RATE;
        }

        if (this.hasInductionUpgrade) {
            useRate += 3 * ENERGY_USE_RATE;

            return 1 + ((useRate - 1) * this.inductionHeat / MAX_INDUCTION_HEAT);
        }

        return useRate;
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayo.electric_furnace");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int menuId, Inventory inventory, Player player) {
        return new ElectricFurnaceMenu(menuId, this, inventory);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.hasInductionUpgrade) {
            output.putInt("induction_heat", this.inductionHeat);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        if (this.hasInductionUpgrade) {
            this.inductionHeat = input.getIntOr("induction_heat", 0);
        }
    }

    @Override
    protected void updateUpgradeState() {
        super.updateUpgradeState();

        this.hasInductionUpgrade = false;
        for (int i = FIRST_UPGRADE_SLOT; i <= LAST_UPGRADE_SLOT; i++) {
            var stack = this.stacks.get(i);
            if (stack.is(Hayo.Items.INDUCTION_UPGRADE)) {
                this.hasInductionUpgrade = true;
                break;
            }
        }
        if (!this.hasInductionUpgrade) {
            this.inductionHeat = 0;
        }

        var mode = ElectricFurnaceMode.NORMAL;
        for (int i = FIRST_UPGRADE_SLOT; i <= LAST_UPGRADE_SLOT; i++) {
            var stack = this.stacks.get(i);
            if (stack.is(Hayo.Items.BLASTING_UPGRADE)) {
                mode = ElectricFurnaceMode.BLASTING;
                break;
            } else if (stack.is(Hayo.Items.SMOKING_UPGRADE)) {
                mode = ElectricFurnaceMode.SMOKING;
                break;
            }
        }
        if (mode != this.mode) {
            this.mode = mode;
            this.resetRecipeProgress();
        }
    }
}

