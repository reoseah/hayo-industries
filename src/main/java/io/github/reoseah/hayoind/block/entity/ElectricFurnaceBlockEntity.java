package io.github.reoseah.hayoind.block.entity;

import io.github.reoseah.hayoind.Hayo;
import io.github.reoseah.hayoind.menu.ClassicProcessingMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.Nullable;

public class ElectricFurnaceBlockEntity extends ProcessingMachineBlockEntity<AbstractCookingRecipe, SingleRecipeInput> {
    public static final int TRANSFER_RATE = 32;
    public static final int ENERGY_USE_RATE = 3;
    public static final int CAPACITY = getEnergyCost(AbstractFurnaceBlockEntity.BURN_TIME_STANDARD);
    public static final int INPUT_SLOT = 0;
    public static final int BATTERY_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int FIRST_UPGRADE_SLOT = 3;
    public static final int UPGRADE_SLOTS = 4;

    protected ElectricFurnaceMode mode = ElectricFurnaceMode.NORMAL;
    protected RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> quickCheck = RecipeManager.createCheck(RecipeType.SMELTING);

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.ELECTRIC_FURNACE, pos, state);
    }

    public static int getEnergyCost(int cookingTime) {
        return cookingTime * ENERGY_USE_RATE * 3 / 4;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected RecipeManager.CachedCheck<SingleRecipeInput, AbstractCookingRecipe> getRecipeCache() {
        return (RecipeManager.CachedCheck<SingleRecipeInput, AbstractCookingRecipe>) this.quickCheck;
    }

    @Override
    protected NonNullList<ItemStack> createInventory() {
        return NonNullList.withSize(7, ItemStack.EMPTY);
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayoind.electric_furnace");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int menuId, Inventory inventory, Player player) {
        return new ClassicProcessingMachineMenu.ElectricFurnaceMenu(menuId, this, inventory);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.mode = getRecipeMode(this.stacks);
        this.quickCheck = RecipeManager.createCheck(this.mode.recipeType);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (this.level instanceof ServerLevel serverLevel) {
            if (slot == INPUT_SLOT) {
                var oldStack = this.stacks.get(slot);

                super.setItem(slot, stack);
                if (!ItemStack.isSameItemSameComponents(stack, oldStack)) {
                    resetRecipe(serverLevel, this, ElectricFurnaceBehavior.INSTANCE);
                }
            } else if (slot >= FIRST_UPGRADE_SLOT) {
                super.setItem(slot, stack);

                var mode = getRecipeMode(this.stacks);
                if (mode != this.mode) {
                    this.mode = mode;
                    this.quickCheck = RecipeManager.createCheck(mode.recipeType);
                    resetRecipe(serverLevel, this, ElectricFurnaceBehavior.INSTANCE);
                }
            }
        }

        super.setItem(slot, stack);
    }

    protected static ElectricFurnaceMode getRecipeMode(NonNullList<ItemStack> stacks) {
        for (int i = FIRST_UPGRADE_SLOT; i < FIRST_UPGRADE_SLOT + UPGRADE_SLOTS; i++) {
            var stack = stacks.get(i);
            if (stack.is(Hayo.Items.BLASTING_UPGRADE)) {
                return ElectricFurnaceMode.BLASTING;
            } else if (stack.is(Hayo.Items.SMOKING_UPGRADE)) {
                return ElectricFurnaceMode.SMOKING;
            }
        }
        return ElectricFurnaceMode.NORMAL;
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, ElectricFurnaceBlockEntity entity) {
        tickChargeFromSlot(entity, BATTERY_SLOT, CAPACITY, TRANSFER_RATE);
        tickProcessing((ServerLevel) level, pos, state, entity, ElectricFurnaceBehavior.INSTANCE);
    }

    public enum ElectricFurnaceBehavior implements SingleProcessingBehavior<AbstractCookingRecipe> {
        INSTANCE;

        @Override
        public int getEnergyUseRate() {
            return ENERGY_USE_RATE;
        }

        @Override
        public int getRecipeEnergy(RecipeHolder<AbstractCookingRecipe> recipe) {
            return getEnergyCost(recipe.value().cookingTime());
        }
    }

    public enum ElectricFurnaceMode {
        NORMAL(RecipeType.SMELTING), BLASTING(RecipeType.BLASTING), SMOKING(RecipeType.SMOKING);

        public final RecipeType<? extends AbstractCookingRecipe> recipeType;

        ElectricFurnaceMode(RecipeType<? extends AbstractCookingRecipe> recipeType) {
            this.recipeType = recipeType;
        }
    }
}

