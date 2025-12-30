package io.github.reoseah.hayo.feature.generator;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.api.energy.ElectricBlocks;
import io.github.reoseah.hayo.base.block.entity.ElectricBlockEntity;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class GeneratorBlockEntity extends ElectricBlockEntity {
    public static final int FUEL_CONSUMPTION_RATE = 2;
    public static final int ENERGY_PER_FUEL_TICK = 5;
    public static final int GENERATION_RATE = FUEL_CONSUMPTION_RATE * ENERGY_PER_FUEL_TICK;
    public static final int CAPACITY = 10000;
    public static final int TRANSFER_RATE = 32;

    @Getter
    protected int fuelEnergyLeft;
    @Getter
    protected int fuelEnergyTotal;

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.GENERATOR, pos, state);
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, GeneratorBlockEntity entity) {
        boolean wasBurning = entity.fuelEnergyLeft > 0;

        if (!wasBurning && entity.canConsumeFuel() && entity.storedEnergy < CAPACITY) {
            entity.tryConsumeFuel();
        }

        if (entity.fuelEnergyLeft > 0) {
            var generation = Math.min(entity.fuelEnergyLeft, GENERATION_RATE);
            entity.fuelEnergyLeft -= generation;
            entity.storedEnergy += generation;
            if (entity.storedEnergy > CAPACITY) {
                entity.storedEnergy = CAPACITY;
            }
            entity.setChanged();
        }

        if (entity.storedEnergy > 0) {
            int sendable = Math.min(entity.storedEnergy, TRANSFER_RATE);
            int sent = ElectricBlocks.trySendToAllSides(sendable, (ServerLevel) level, pos);
            if (sent > 0) {
                entity.storedEnergy -= sent;
                entity.setChanged();
            }
        }

        boolean isBurning = entity.fuelEnergyLeft > 0;
        if (isBurning != wasBurning) {
            level.setBlockAndUpdate(pos, state.setValue(GeneratorBlock.LIT, isBurning));
        }
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayo.generator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int menuId, Inventory playerInventory, Player player) {
        return new GeneratorMenu(menuId, this, playerInventory);
    }

    @Override
    protected NonNullList<ItemStack> createInventory() {
        return NonNullList.withSize(1, ItemStack.EMPTY);
    }

    @Override
    protected int getEnergyCapacity() {
        return CAPACITY;
    }

    @Override
    protected int getEnergyTransferRate() {
        return TRANSFER_RATE;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("fuel_energy_left", this.fuelEnergyLeft);
        output.putInt("fuel_energy_total", this.fuelEnergyTotal);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.fuelEnergyLeft = input.getIntOr("fuel_energy_left", 0);
        this.fuelEnergyTotal = input.getIntOr("fuel_energy_total", 0);
    }

    protected boolean canConsumeFuel() {
        return this.level != null && //
                FuelValues.vanillaBurnTimes(this.level.registryAccess(), FeatureFlags.DEFAULT_FLAGS).isFuel(this.getItem(0));
    }

    protected void tryConsumeFuel() {
        if (this.level == null) {
            return;
        }
        int fuelValue = FuelValues.vanillaBurnTimes(this.level.registryAccess(), FeatureFlags.DEFAULT_FLAGS).burnDuration(this.getItem(0));
        if (fuelValue <= 0) {
            return;
        }
        this.getItem(0).shrink(1);
        this.fuelEnergyTotal = this.fuelEnergyLeft = fuelValue * ENERGY_PER_FUEL_TICK;
        this.setChanged();
    }

}
