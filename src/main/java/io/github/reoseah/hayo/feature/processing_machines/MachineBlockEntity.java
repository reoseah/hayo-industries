package io.github.reoseah.hayo.feature.processing_machines;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.base.block.OrientableMachineBlock;
import io.github.reoseah.hayo.base.block.entity.ElectricBlockEntity;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

public abstract class MachineBlockEntity<R extends Recipe<I>, I extends RecipeInput> extends ElectricBlockEntity {
    @Getter
    private int recipeUsedEnergy;
    @Getter
    private int recipeTotalEnergy;
    private int capacityFromUpgrades = 0;
    @Getter
    private int overclockCount = 0;

    @Nullable
    private ResourceKey<Recipe<?>> lastRecipe;

    public MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static <R extends Recipe<I>, I extends RecipeInput> void tickProcessing(ServerLevel level, BlockPos pos, BlockState state, MachineBlockEntity<R, I> entity) {
        boolean wasProcessing = entity.recipeUsedEnergy > 0;

        var input = entity.createRecipeInput(entity.stacks);
        if (input.isEmpty()) {
            if (entity.recipeUsedEnergy > 0) {
                entity.recipeUsedEnergy = entity.recipeTotalEnergy = 0;
                entity.setChanged();
            }
        } else {
            var recipeHolder = entity.findMatchingRecipe(level, input);

            int energyUseRate = entity.getEnergyUseRate();
            boolean hasEnergy = entity.storedEnergy >= energyUseRate;
            if (hasEnergy && entity.canCraft(level.registryAccess(), recipeHolder, input, entity.stacks)) {
                int usable = Math.min(Math.min(energyUseRate, entity.recipeTotalEnergy - entity.recipeUsedEnergy), entity.storedEnergy);

                entity.storedEnergy -= usable;
                entity.recipeUsedEnergy += usable;

                if (entity.recipeUsedEnergy >= entity.recipeTotalEnergy) {
                    entity.craft(level.registryAccess(), recipeHolder, input, entity.stacks);
                    entity.resetRecipeProgress();
                }

                entity.setChanged();
            } else {
                entity.recipeUsedEnergy = Mth.clamp(entity.recipeUsedEnergy - 2 * energyUseRate, 0, entity.recipeTotalEnergy);
                entity.setChanged();
            }
        }

        boolean isProcessing = entity.recipeUsedEnergy > 0;
        if (wasProcessing != isProcessing) {
            level.setBlockAndUpdate(pos, state.setValue(OrientableMachineBlock.LIT, isProcessing));
        }
    }

    public static ContainerData createData(MachineBlockEntity<?, ?> entity) {
        return new ContainerData() {
            @Override
            public int getCount() {
                return 7;
            }

            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> entity.getStoredEnergy() & 0xFFFF;
                    case 1 -> entity.getStoredEnergy() >>> 16;
                    case 2 -> entity.getRecipeUsedEnergy();
                    case 3 -> entity.getRecipeTotalEnergy();
                    case 4 -> entity.getEnergyCapacity() & 0xFFFF;
                    case 5 -> entity.getEnergyCapacity() >>> 16;
                    case 6 -> entity.getOverclockCount();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }
        };
    }

    protected abstract RecipeType<R> getRecipeType();

    protected abstract int getDefaultCapacity();

    protected abstract int getDefaultEnergyUseRate();

    protected abstract int getDefaultRecipeEnergy(RecipeHolder<R> holder);

    protected abstract int getSlotCount();

    protected abstract boolean isInputSlot(int slot);

    protected abstract int getFirstUpgradeSlot();

    protected abstract int getLastUpgradeSlot();

    protected abstract I createRecipeInput(NonNullList<ItemStack> items);

    protected abstract boolean canCraft(RegistryAccess registryAccess, @Nullable RecipeHolder<R> recipe, I recipeInput, NonNullList<ItemStack> items);

    protected abstract void craft(RegistryAccess registryAccess, RecipeHolder<R> recipe, I input, NonNullList<ItemStack> items);

    @Override
    public int getEnergyCapacity() {
        return this.getDefaultCapacity() + this.capacityFromUpgrades;
    }

    public int getEnergyUseRate() {
        return this.getDefaultEnergyUseRate() * (1 + this.overclockCount);
    }

    public int getRecipeTotalEnergy(RecipeHolder<R> holder) {
        return this.getDefaultRecipeEnergy(holder) * (100 + 25 * this.overclockCount) / 100;
    }

    @Override
    protected NonNullList<ItemStack> createInventory() {
        return NonNullList.withSize(this.getSlotCount(), ItemStack.EMPTY);
    }

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

        this.updateUpgradeState();
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (this.level instanceof ServerLevel serverLevel) {
            if (this.isInputSlot(slot)) {
                var oldStack = this.stacks.get(slot);

                super.setItem(slot, stack);

                if (!ItemStack.isSameItemSameComponents(stack, oldStack)) {
                    this.resetRecipeProgress();
                }
                return;
            } else if (slot >= this.getFirstUpgradeSlot() && slot <= this.getLastUpgradeSlot()) {
                super.setItem(slot, stack);

                this.updateUpgradeState();
                return;
            }
        }
        super.setItem(slot, stack);
    }

    public @Nullable RecipeHolder<R> findMatchingRecipe(ServerLevel level, I input) {
        var recipeManager = level.recipeAccess();
        var optional = recipeManager.getRecipeFor(this.getRecipeType(), input, level, this.lastRecipe);
        if (optional.isPresent()) {
            this.lastRecipe = optional.get().id();
        }
        return optional.orElse(null);
    }

    @MustBeInvokedByOverriders
    protected void updateUpgradeState() {
        int capacityFromUpgrades = 0;
        int overclockCount = 0;

        int first = this.getFirstUpgradeSlot();
        int last = this.getLastUpgradeSlot();
        for (int i = first; i <= last; i++) {
            var stack = stacks.get(i);
            if (stack.is(Hayo.Items.CAPACITOR_UPGRADE)) {
                capacityFromUpgrades += 10000;
            } else if (stack.is(Hayo.Items.OVERCLOCK_UPGRADE)) {
                overclockCount += 1;
            }
        }

        this.capacityFromUpgrades = capacityFromUpgrades;
        if (this.storedEnergy > this.getDefaultCapacity()) {
            this.storedEnergy = this.getDefaultCapacity();
        }
        if (overclockCount != this.overclockCount) {
            this.overclockCount = overclockCount;
            this.updateRecipeCost();
        }
    }

    protected void resetRecipeProgress() {
        if (this.level instanceof ServerLevel serverLevel) {
            var input = this.createRecipeInput(this.stacks);
            var recipeHolder = this.findMatchingRecipe(serverLevel, input);
            if (recipeHolder != null) {
                this.recipeTotalEnergy = this.getRecipeTotalEnergy(recipeHolder);
                this.recipeUsedEnergy = 0;
            } else {
                this.recipeUsedEnergy = this.recipeTotalEnergy = 0;
            }
        }
    }

    protected void updateRecipeCost() {
        if (this.level instanceof ServerLevel serverLevel) {
            var input = this.createRecipeInput(this.stacks);
            var recipeHolder = this.findMatchingRecipe(serverLevel, input);
            if (recipeHolder != null) {
                this.recipeTotalEnergy = this.getRecipeTotalEnergy(recipeHolder);
            } else {
                this.recipeTotalEnergy = 0;
            }
        }
    }

    protected static boolean canInsertToSlot(NonNullList<ItemStack> items, ItemStack recipeOutput, int slot) {
        var outputStack = items.get(slot);
        if (outputStack.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(outputStack, recipeOutput)) {
            return false;
        }

        return outputStack.getCount() + recipeOutput.getCount() <= recipeOutput.getMaxStackSize();
    }
}
