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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ElectricFurnaceBlockEntity extends ProcessingMachineBlockEntity<SmeltingRecipe, SingleRecipeInput> {
    public static final int TRANSFER_RATE = 32;
    public static final int ENERGY_USE_RATE = 3;
    public static final int CAPACITY = AbstractFurnaceBlockEntity.BURN_TIME_STANDARD * ENERGY_USE_RATE * 3 / 4;
    public static final int INPUT_SLOT = 0;
    public static final int BATTERY_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.ELECTRIC_FURNACE, RecipeType.SMELTING, pos, state);
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
    public void setItem(int slot, ItemStack stack) {
        if (slot == INPUT_SLOT) {
            var oldStack = this.stacks.get(slot);

            super.setItem(slot, stack);
            if (!ItemStack.isSameItemSameComponents(stack, oldStack) && this.level instanceof ServerLevel serverLevel) {
                resetRecipe(serverLevel, this, ElectricFurnaceBehavior.INSTANCE);
            }
        } else {
            super.setItem(slot, stack);
        }
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, ElectricFurnaceBlockEntity entity) {
        tickChargeFromSlot(entity, BATTERY_SLOT, CAPACITY, TRANSFER_RATE);
        tickProcessing((ServerLevel) level, pos, state, entity, ElectricFurnaceBehavior.INSTANCE);
    }

    public enum ElectricFurnaceBehavior implements SingleProcessingBehavior<SmeltingRecipe> {
        INSTANCE;

        @Override
        public int getEnergyUseRate() {
            return ENERGY_USE_RATE;
        }

        @Override
        public int getRecipeEnergy(RecipeHolder<SmeltingRecipe> recipe) {
            return recipe.value().cookingTime() * ENERGY_USE_RATE * 3 / 4;
        }
    }
}

