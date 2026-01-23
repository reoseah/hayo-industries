package io.github.reoseah.hayo.base.block.entity;

import io.github.reoseah.hayo.feature.energy.item.EnergyComponents;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class ElectricBlockEntity extends HayoContainerBlockEntity {
    @Getter
    protected int storedEnergy;
    @Getter
    protected int inputPerTick;

    public ElectricBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract int getEnergyCapacity();

    protected abstract int getEnergyTransferLimit();

    protected boolean doesStoredEnergyPersist() {
        return true;
    }

    @Override
    @MustBeInvokedByOverriders
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.doesStoredEnergyPersist()) {
            output.putInt("stored_energy", this.storedEnergy);
        }
    }

    @Override
    @MustBeInvokedByOverriders
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        if (this.doesStoredEnergyPersist()) {
            this.storedEnergy = input.getIntOr("stored_energy", 0);
        }
    }

    public int getReceivableEnergy() {
        return Math.min(this.getEnergyCapacity() - this.storedEnergy, this.getEnergyTransferLimit() - this.inputPerTick);
    }

    public int receiveEnergy(int amount) {
        int change = Math.min(amount, this.getReceivableEnergy());
        if (change > 0) {
            this.storedEnergy += change;
            this.inputPerTick += change;
            this.setChanged();
        }
        return change;
    }

    public void chargeFromSlot(int slot) {
        int limit = this.getReceivableEnergy();
        if (limit > 0) {
            int change = EnergyComponents.discharge(limit, this.getItem(slot));
            if (change > 0) {
                this.storedEnergy += change;
                this.inputPerTick += change;
                this.setChanged();
            }
        }
    }

    protected void resetEnergyPerTick() {
        this.inputPerTick = 0;
    }
}
