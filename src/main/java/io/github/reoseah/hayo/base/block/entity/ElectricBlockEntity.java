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
    protected int energyPerTick;

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

    public int getReceivableEnergy() {
        return Math.min(this.getEnergyCapacity() - this.storedEnergy, this.getEnergyTransferRate() - this.energyPerTick);
    }

    public int receiveEnergy(int amount) {
        int change = Math.min(amount, this.getReceivableEnergy());
        if (change > 0) {
            this.storedEnergy += change;
            this.energyPerTick += change;
            this.setChanged();
        }
        return change;
    }

    public void chargeFromSlot(int slot) {
        int limit = this.getReceivableEnergy();
        if (limit > 0) {
            int change = EnergyComponents.discharge(limit, getItem(slot));
            if (change > 0) {
                this.storedEnergy += change;
                this.energyPerTick += change;
                this.setChanged();
            }
        }
    }

    protected void onTickEnd() {
        this.energyPerTick = 0;
    }
}
