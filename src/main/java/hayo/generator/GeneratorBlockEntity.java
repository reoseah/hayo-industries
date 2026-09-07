package hayo.generator;

import hayo.Hayo;
import hayo.common.block.HorizontalDirectionalElectricalBlock;
import hayo.common.blockentity.SimpleContainerBlockEntity;
import hayo.energy.block.EnergyAPI;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
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
import org.jspecify.annotations.Nullable;

public class GeneratorBlockEntity extends SimpleContainerBlockEntity implements WorldlyContainer, MenuProvider {
    public static final int FUEL_CONSUMPTION_RATE = 2;
    public static final int ENERGY_PER_FUEL_TICK = 5;
    public static final int GENERATION_RATE = FUEL_CONSUMPTION_RATE * ENERGY_PER_FUEL_TICK;
    public static final int CAPACITY = 10000;
    public static final int TRANSFER_LIMIT = 32;

    @Getter
    protected int storedEnergy;
    @Getter
    protected int fuelEnergyLeft;
    @Getter
    protected int fuelEnergyTotal;

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.GENERATOR, pos, state, NonNullList.withSize(1, ItemStack.EMPTY));
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, GeneratorBlockEntity entity) {
        boolean wasBurning = entity.fuelEnergyLeft > 0;

        if (!wasBurning && entity.canConsumeFuel() && entity.storedEnergy < CAPACITY) {
            entity.tryConsumeFuel();
        }

        if (entity.fuelEnergyLeft > 0) {
            var generation = Math.max(1, Math.min(Math.min(entity.fuelEnergyLeft, GENERATION_RATE), CAPACITY - entity.storedEnergy));
            entity.fuelEnergyLeft -= generation;
            entity.storedEnergy += generation;
            if (entity.storedEnergy > CAPACITY) {
                entity.storedEnergy = CAPACITY;
            }
            entity.setChanged();
        }

        if (entity.storedEnergy > 0) {
            int sendable = Math.min(entity.storedEnergy, TRANSFER_LIMIT);
            int sent = EnergyAPI.trySendToAllSides(sendable, (ServerLevel) level, pos);
            if (sent > 0) {
                entity.storedEnergy -= sent;
                entity.setChanged();
            }
        }

        boolean isBurning = entity.fuelEnergyLeft > 0;
        if (isBurning != wasBurning) {
            level.setBlockAndUpdate(pos, state.setValue(HorizontalDirectionalElectricalBlock.LIT, isBurning));
        }
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayo.generator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new GeneratorMenu(containerId, this, playerInventory);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("stored_energy", this.storedEnergy);
        output.putInt("fuel_energy_left", this.fuelEnergyLeft);
        output.putInt("fuel_energy_total", this.fuelEnergyTotal);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.storedEnergy = input.getIntOr("stored_energy", 0);
        this.fuelEnergyLeft = input.getIntOr("fuel_energy_left", 0);
        this.fuelEnergyTotal = input.getIntOr("fuel_energy_total", 0);
    }

    protected boolean canConsumeFuel() {
        return this.level != null //
                && !this.getItem(0).is(Hayo.ItemTags.DISABLED_GENERATOR_FUELS) //
                && FuelValues.vanillaBurnTimes(this.level.registryAccess(), FeatureFlags.DEFAULT_FLAGS).isFuel(this.getItem(0));
    }

    protected void tryConsumeFuel() {
        if (this.level == null) {
            return;
        }
        var fuel = this.getItem(0);
        if (fuel.is(Hayo.ItemTags.DISABLED_GENERATOR_FUELS)) {
            return;
        }
        int fuelValue = FuelValues.vanillaBurnTimes(this.level.registryAccess(), FeatureFlags.DEFAULT_FLAGS).burnDuration(fuel);
        if (fuelValue <= 0) {
            return;
        }

        var fuelRemainder = fuel.getCraftingRemainder();
        fuel.shrink(1);
        if (fuel.isEmpty() && fuelRemainder != null) {
            this.setItem(0, fuelRemainder.create());
        }

        this.fuelEnergyTotal = this.fuelEnergyLeft = fuelValue * ENERGY_PER_FUEL_TICK;
        this.setChanged();
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return FuelValues.vanillaBurnTimes(this.level.registryAccess(), FeatureFlags.DEFAULT_FLAGS).isFuel(stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return !FuelValues.vanillaBurnTimes(this.level.registryAccess(), FeatureFlags.DEFAULT_FLAGS).isFuel(stack);
    }

}
