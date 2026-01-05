package io.github.reoseah.hayo.feature.energy_storages;

import io.github.reoseah.hayo.base.block.entity.ElectricBlockEntity;
import io.github.reoseah.hayo.feature.energy.ElectricItems;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class EnergyStorageBlockEntity extends ElectricBlockEntity {
    @Getter
    protected float averageEnergyPerTick;

    public EnergyStorageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected NonNullList<ItemStack> createInventory() {
        return NonNullList.withSize(2, ItemStack.EMPTY);
    }

    @Override
    protected void onTickEnd() {
        this.averageEnergyPerTick = Mth.lerp(0.05F, this.averageEnergyPerTick, this.energyPerTick);
        super.onTickEnd();
    }

    @SuppressWarnings("unused")
    public static void tickServer(Level level, BlockPos pos, BlockState state, EnergyStorageBlockEntity entity) {
        entity.chargeFromSlot(0);

        int discharge = ElectricItems.tryCharge(entity.getEnergyTransferRate(), entity, 1);
        if (discharge > 0) {
            entity.storedEnergy -= discharge;
            entity.energyPerTick -= discharge;
            entity.setChanged();
        }
        entity.onTickEnd();
    }
}
