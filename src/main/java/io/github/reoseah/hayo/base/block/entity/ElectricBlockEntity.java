package io.github.reoseah.hayo.base.block.entity;

import io.github.reoseah.hayo.api.energy.ElectricItems;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class ElectricBlockEntity extends HayoContainerBlockEntity {
    @Getter
    protected int storedEnergy;
    @Getter
    protected int energyPerTick;
    @Getter
    protected float averageEnergyPerTick;

    public ElectricBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract int getEnergyCapacity();

    protected abstract int getEnergyTransferRate();

    @Override
    @MustBeInvokedByOverriders
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("stored_energy", this.storedEnergy);
    }

    @Override
    @MustBeInvokedByOverriders
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.storedEnergy = input.getIntOr("stored_energy", 0);
    }

    public static void tickChargeFromSlot(ElectricBlockEntity entity, int slot, int capacity, int transferRate) {
        int max = Math.min(capacity - entity.storedEnergy, transferRate);
        if (max != 0) {
            int charge = ElectricItems.tryDischarge(max, entity, slot);
            if (charge > 0) {
                entity.storedEnergy += charge;
                entity.energyPerTick += charge;
                entity.setChanged();
            }
        }
    }

    public static void tickEnergyPerTick(ElectricBlockEntity entity) {
        entity.averageEnergyPerTick = Mth.lerp(0.05F, entity.averageEnergyPerTick, entity.energyPerTick);
        entity.energyPerTick = 0;
    }

    public int getReceivableEnergy(ServerLevel level, BlockPos pos, Direction side) {
        return Math.min(this.getEnergyCapacity() - this.storedEnergy, this.getEnergyTransferRate()); // todo: limit energy per tick
    }

    public int receiveEnergy(int amount, ServerLevel level, BlockPos pos, Direction side) {
        int change = Math.min(amount, Math.min(this.getEnergyCapacity() - this.storedEnergy, this.getEnergyTransferRate()));
        this.storedEnergy += change;
        this.energyPerTick += change;
        return change;
    }
}
