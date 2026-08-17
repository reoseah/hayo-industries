package hayo.block.entity;

import hayo.item.components.EnergyComponents;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class SimpleElectricBlockEntity extends SimpleContainerBlockEntity {
    @Getter
    @Setter
    protected int storedEnergy;
    /// Energy accepted, limits energy input per tick to 32/128/512/whatever.
    /// Classes extending this should reset this every tick by calling [#resetEnergyPerTick].
    @Getter
    @Setter
    protected int inputPerTick;

    public SimpleElectricBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, NonNullList<ItemStack> stacks) {
        super(type, pos, state, stacks);
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
        if (change <= 0) {
            return 0;
        }
        this.storedEnergy += change;
        this.inputPerTick += change;
        this.setChanged();

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
