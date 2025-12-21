package io.github.reoseah.hayoind.block.entity;

import io.github.reoseah.hayoind.Hayo;
import io.github.reoseah.hayoind.menu.ProcessingMachineMenu;
import io.github.reoseah.hayoind.recipe.SimpleElectricRecipe;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MaceratorBlockEntity extends ProcessingMachineBlockEntity<SimpleElectricRecipe, SingleRecipeInput> {
    public static final int TRANSFER_RATE = 32;
    public static final int ENERGY_USE_RATE = 2;
    public static final int CAPACITY = 10 * 20 * ENERGY_USE_RATE; // 10s * 20tick/s * 2e/tick = 400e

    public static final int SLOTS = SlotHelper.Classic.SLOTS;
    public static final int INPUT_SLOT = SlotHelper.Classic.INPUT_SLOT;
    public static final int BATTERY_SLOT = 1;
    public static final int OUTPUT_SLOT = SlotHelper.Classic.OUTPUT_SLOT;
    public static final int FIRST_UPGRADE_SLOT = SlotHelper.Classic.FIRST_UPGRADE_SLOT;
    public static final int LAST_UPGRADE_SLOT = SlotHelper.Classic.LAST_UPGRADE_SLOT;

    public MaceratorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.MACERATOR, pos, state);
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, MaceratorBlockEntity entity) {
        tickChargeFromSlot(entity, BATTERY_SLOT, CAPACITY, TRANSFER_RATE);
        tickProcessing((ServerLevel) level, pos, state, entity);
    }

    @Override
    protected NonNullList<ItemStack> createInventory() {
        return NonNullList.withSize(7, ItemStack.EMPTY);
    }

    @Override
    protected RecipeType<SimpleElectricRecipe> getRecipeType() {
        return Hayo.RecipeTypes.MACERATING;
    }

    @Override
    protected SlotHelper<SimpleElectricRecipe, SingleRecipeInput> getSlotHelper() {
        return SlotHelper.classic();
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
    public int getDefaultRecipeEnergy(RecipeHolder<SimpleElectricRecipe> recipe) {
        return recipe.value().processingEnergy();
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayoind.macerator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int menuId, Inventory inventory, Player player) {
        return new ProcessingMachineMenu.MaceratorMenu(menuId, this, inventory);
    }
}
