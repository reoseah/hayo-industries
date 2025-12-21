package io.github.reoseah.hayoind.block.entity;

import io.github.reoseah.hayoind.Hayo;
import io.github.reoseah.hayoind.block.OrientableMachineBlock;
import io.github.reoseah.hayoind.recipe.SecondaryOutputElectricRecipe;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
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
    private int recipeUsedEnergy;
    @Getter
    private int recipeTotalEnergy;
    private int capacityFromUpgrades = 0;
    @Getter
    private int overclockCount = 0;

    @Nullable
    private ResourceKey<Recipe<?>> lastRecipe;

    public ProcessingMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract RecipeType<R> getRecipeType();

    protected abstract SlotHelper<R, I> getSlotHelper();

    protected abstract int getDefaultCapacity();

    protected abstract int getDefaultEnergyUseRate();

    protected abstract int getDefaultRecipeEnergy(RecipeHolder<R> holder);

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
        return NonNullList.withSize(this.getSlotHelper().getSlots(), ItemStack.EMPTY);
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
            var slotHelper = this.getSlotHelper();
            if (slotHelper.isInputSlot(slot)) {
                var oldStack = this.stacks.get(slot);

                super.setItem(slot, stack);

                if (!ItemStack.isSameItemSameComponents(stack, oldStack)) {
                    this.resetRecipeProgress();
                }
                return;
            } else if (slot >= slotHelper.getFirstUpgradeSlot() && slot <= slotHelper.getLastUpgradeSlot()) {
                super.setItem(slot, stack);

                this.updateUpgradeState();
                return;
            }
        }
        super.setItem(slot, stack);
    }

    @MustBeInvokedByOverriders
    protected void updateUpgradeState() {
        var slots = this.getSlotHelper();
        int firstSlot = slots.getFirstUpgradeSlot();
        int lastSlot = slots.getLastUpgradeSlot();

        int capacityFromUpgrades = 0;
        int overclockCount = 0;
        for (int i = firstSlot; i <= lastSlot; i++) {
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
            var input = this.getSlotHelper().createRecipeInput(this.stacks);
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
            var input = this.getSlotHelper().createRecipeInput(this.stacks);
            var recipeHolder = this.findMatchingRecipe(serverLevel, input);
            if (recipeHolder != null) {
                this.recipeTotalEnergy = this.getRecipeTotalEnergy(recipeHolder);
            } else {
                this.recipeTotalEnergy = 0;
            }
        }
    }

    public @Nullable RecipeHolder<R> findMatchingRecipe(ServerLevel level, I input) {
        var recipeManager = level.recipeAccess();
        var optional = recipeManager.getRecipeFor(this.getRecipeType(), input, level, this.lastRecipe);
        if (optional.isPresent()) {
            this.lastRecipe = optional.get().id();
        }
        return optional.orElse(null);
    }

    public static <R extends Recipe<I>, I extends RecipeInput> void tickProcessing(ServerLevel level, BlockPos pos, BlockState state, ProcessingMachineBlockEntity<R, I> entity) {
        boolean wasProcessing = entity.recipeUsedEnergy > 0;

        var slotHelper = entity.getSlotHelper();
        var input = slotHelper.createRecipeInput(entity.stacks);
        if (input.isEmpty()) {
            if (entity.recipeUsedEnergy > 0) {
                entity.recipeUsedEnergy = entity.recipeTotalEnergy = 0;
                entity.setChanged();
            }
        } else {
            var recipeHolder = entity.findMatchingRecipe(level, input);

            int energyUseRate = entity.getEnergyUseRate();
            boolean hasEnergy = entity.storedEnergy >= energyUseRate;
            if (hasEnergy && slotHelper.canCraft(level.registryAccess(), recipeHolder, input, entity.stacks)) {
                int usable = Math.min(Math.min(energyUseRate, entity.recipeTotalEnergy - entity.recipeUsedEnergy), entity.storedEnergy);

                entity.storedEnergy -= usable;
                entity.recipeUsedEnergy += usable;

                if (entity.recipeUsedEnergy >= entity.recipeTotalEnergy) {
                    slotHelper.craft(level.registryAccess(), recipeHolder, input, entity.stacks);
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

    public interface SlotHelper<R extends Recipe<I>, I extends RecipeInput> {
        int getSlots();

        boolean isInputSlot(int slot);

        I createRecipeInput(NonNullList<ItemStack> items);

        boolean canCraft(RegistryAccess registryAccess, @Nullable RecipeHolder<R> recipe, I recipeInput, NonNullList<ItemStack> items);

        void craft(RegistryAccess registryAccess, RecipeHolder<R> recipe, I input, NonNullList<ItemStack> items);

        int getFirstUpgradeSlot();

        int getLastUpgradeSlot();

        /// Returns a helper for "classic" machines with one input, one output and 4 upgrade slots.
        @SuppressWarnings("unchecked")
        static <R extends Recipe<SingleRecipeInput>> SlotHelper<R, SingleRecipeInput> classic() {
            return (SlotHelper<R, SingleRecipeInput>) Classic.INSTANCE;
        }

        @SuppressWarnings("unchecked")
        static <R extends Recipe<SingleRecipeInput>> SlotHelper<R, SingleRecipeInput> classicWithExtraOutput() {
            return (SlotHelper<R, SingleRecipeInput>) ClassicWithExtraOutput.INSTANCE;
        }

        enum Classic implements SlotHelper<Recipe<SingleRecipeInput>, SingleRecipeInput> {
            INSTANCE;

            public static final int INPUT_SLOT = 0;
            public static final int OUTPUT_SLOT = 2;
            public static final int FIRST_UPGRADE_SLOT = 3;
            public static final int LAST_UPGRADE_SLOT = 6;
            public static final int SLOTS = 7;

            @Override
            public int getSlots() {
                return SLOTS;
            }

            @Override
            public boolean isInputSlot(int slot) {
                return slot == INPUT_SLOT;
            }

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
                return canInsertToSlot(items, recipeOutput, OUTPUT_SLOT);
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

            @Override
            public int getFirstUpgradeSlot() {
                return FIRST_UPGRADE_SLOT;
            }

            @Override
            public int getLastUpgradeSlot() {
                return LAST_UPGRADE_SLOT;
            }
        }

        enum ClassicWithExtraOutput implements SlotHelper<SecondaryOutputElectricRecipe, SingleRecipeInput> {
            INSTANCE;

            public static final int SLOTS = 8;
            public static final int INPUT_SLOT = 0;
            public static final int OUTPUT_SLOT = 2;
            public static final int SECONDARY_OUTPUT_SLOT = 3;
            public static final int FIRST_UPGRADE_SLOT = 4;
            public static final int LAST_UPGRADE_SLOT = 7;

            @Override
            public int getSlots() {
                return SLOTS;
            }

            @Override
            public boolean isInputSlot(int slot) {
                return slot == INPUT_SLOT;
            }

            @Override
            public SingleRecipeInput createRecipeInput(NonNullList<ItemStack> items) {
                return new SingleRecipeInput(items.get(INPUT_SLOT));
            }

            @Override
            public boolean canCraft(RegistryAccess registryAccess, @Nullable RecipeHolder<SecondaryOutputElectricRecipe> recipe, SingleRecipeInput input, NonNullList<ItemStack> items) {
                if (recipe == null || input.isEmpty()) {
                    return false;
                }

                var recipeOutput = recipe.value().assemble(input, registryAccess);
                return canInsertToSlot(items, recipeOutput, OUTPUT_SLOT);
                // TODO: check additional slot
            }

            @Override
            public void craft(RegistryAccess registryAccess, RecipeHolder<SecondaryOutputElectricRecipe> recipe, SingleRecipeInput input, NonNullList<ItemStack> items) {
                var recipeOutput = recipe.value().assemble(input, registryAccess);
                var outputStack = items.get(OUTPUT_SLOT);

                if (outputStack.isEmpty()) {
                    items.set(OUTPUT_SLOT, recipeOutput);
                } else {
                    outputStack.grow(recipeOutput.getCount());
                }
                // TODO: insert secondary output

                var inputStack = items.get(INPUT_SLOT);
                inputStack.shrink(1);
            }

            @Override
            public int getFirstUpgradeSlot() {
                return FIRST_UPGRADE_SLOT;
            }

            @Override
            public int getLastUpgradeSlot() {
                return LAST_UPGRADE_SLOT;
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
