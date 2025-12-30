package io.github.reoseah.hayo.feature.processing_machines.macerator;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.processing_machines.BasicMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MaceratorBlockEntity extends BasicMachineBlockEntity<MaceratingRecipe> {
    public static final int TRANSFER_RATE = 32;
    public static final int ENERGY_USE_RATE = 2;
    public static final int CAPACITY = 10 * 20 * ENERGY_USE_RATE; // 10s * 20tick/s * 2e/tick = 400e

    public MaceratorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.MACERATOR, pos, state);
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, MaceratorBlockEntity entity) {
        tickChargeFromSlot(entity, BATTERY_SLOT, CAPACITY, TRANSFER_RATE);
        tickProcessing((ServerLevel) level, pos, state, entity);
    }

    @Override
    protected RecipeType<MaceratingRecipe> getRecipeType() {
        return Hayo.RecipeTypes.MACERATING;
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
    protected int getEnergyTransferRate() {
        return TRANSFER_RATE;
    }

    @Override
    public int getDefaultRecipeEnergy(RecipeHolder<MaceratingRecipe> recipe) {
        return recipe.value().processingEnergy();
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayo.macerator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int menuId, Inventory inventory, Player player) {
        return new MaceratorMenu(menuId, this, inventory);
    }
}
