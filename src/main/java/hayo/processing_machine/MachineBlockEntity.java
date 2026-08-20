package hayo.processing_machine;

import hayo.common.blockentity.SimpleElectricBlockEntity;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
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
            var recipeHolder = this.updateMatchingRecipe(level, input);

            int recipeTotalEnergy = this.getEnergyCost(recipeHolder);
            if (this.hasEnoughEnergyToProgress() && this.canCraft(level.registryAccess(), recipeHolder, input)) {
                int usable = Math.min(Math.min(recipeTotalEnergy - this.recipeProgress, this.getEnergyUseRate()), this.storedEnergy);

                this.storedEnergy -= usable;
                this.recipeProgress += usable;
                madeProgress = true;

                if (this.recipeProgress >= recipeTotalEnergy) {
                    this.craft(level.registryAccess(), recipeHolder, input);
                    this.recipeProgress = 0;
                }

                this.setChanged();
            } else if (this.recipeProgress > 0) {
                this.recipeProgress = Mth.clamp(this.recipeProgress - 2 * this.getEnergyUseRate(), 0, recipeTotalEnergy);
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

    public @Nullable RecipeHolder<R> updateMatchingRecipe(ServerLevel level, I input) {
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

    public interface MachineContainerData extends ContainerData {
        int SIZE = 9;

        default int energy() {
            return (this.get(1) << 16) | (this.get(0) & 0xFFFF);
        }

        default int capacity() {
            return (this.get(3) << 16) | (this.get(2) & 0xFFFF);
        }

        default int progressEnergy() {
            return (this.get(5) << 16) | (this.get(4) & 0xFFFF);
        }

        default int recipeCost() {
            return (this.get(7) << 16) | (this.get(6) & 0xFFFF);
        }

        default int energyUseRate() {
            return this.get(8);
        }

        default float recipeDuration() {
            return Mth.ceil(this.recipeCost() / (float) this.energyUseRate()) / 20F;
        }

        class Simple extends SimpleContainerData implements MachineContainerData {
            public Simple() {
                super(SIZE);
            }
        }

        record Entity(MachineBlockEntity<?, ?> entity) implements MachineContainerData {
            @Override
            public int getCount() {
                return SIZE;
            }

            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> this.entity.getStoredEnergy() & 0xFFFF;
                    case 1 -> this.entity.getStoredEnergy() >>> 16;
                    case 2 -> this.entity.getEnergyCapacity() & 0xFFFF;
                    case 3 -> this.entity.getEnergyCapacity() >>> 16;
                    case 4 -> this.entity.getRecipeProgress() & 0xFFFF;
                    case 5 -> this.entity.getRecipeProgress() >>> 16;
                    case 6 -> this.entity.getLastOrDefaultRecipeCost() & 0xFFFF;
                    case 7 -> this.entity.getLastOrDefaultRecipeCost() >>> 16;
                    case 8 -> this.entity.getEnergyUseRate();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }
        }
    }
}
