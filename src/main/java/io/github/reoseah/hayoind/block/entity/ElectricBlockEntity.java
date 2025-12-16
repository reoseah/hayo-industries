package io.github.reoseah.hayoind.block.entity;

import io.github.reoseah.hayoind.item.ElectricItems;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class ElectricBlockEntity extends HayoContainerBlockEntity {
    @Getter
    protected int storedEnergy;

    public ElectricBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("stored_energy", this.storedEnergy);
    }

    @Override
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
                entity.setChanged();
            }
        }
    }
}
