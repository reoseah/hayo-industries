package io.github.reoseah.hayo.feature.energy_storages;

import io.github.reoseah.hayo.base.block.DirectionalMachineBlock;
import io.github.reoseah.hayo.base.block.entity.ElectricBlockEntity;
import io.github.reoseah.hayo.feature.energy.ElectricBlocks;
import io.github.reoseah.hayo.feature.energy.ElectricItems;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public abstract class EnergyStorageBlockEntity extends ElectricBlockEntity implements WorldlyContainer {
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

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return switch (direction) {
            case UP -> new int[]{0};
            case DOWN -> new int[]{0, 1};
            default -> new int[]{1};
        };
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return switch (direction) {
            case UP -> !ElectricItems.canDischarge(stack);
            case DOWN -> slot == 0 ? !ElectricItems.canDischarge(stack) : !ElectricItems.canCharge(stack);
            default -> !ElectricItems.canCharge(stack);
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return switch (direction) {
            case UP -> ElectricItems.canDischarge(stack);
            case DOWN -> slot == 0 ? ElectricItems.canDischarge(stack) : ElectricItems.canCharge(stack);
            case null, default -> ElectricItems.canCharge(stack);
        };
    }

    @SuppressWarnings("unused")
    public static void tickServer(Level level, BlockPos pos, BlockState state, EnergyStorageBlockEntity entity) {
        entity.chargeFromSlot(0);

        int discharge = ElectricItems.tryCharge(Math.min(entity.storedEnergy, entity.getEnergyTransferRate()), entity, 1);
        if (discharge > 0) {
            entity.storedEnergy -= discharge;
            entity.energyPerTick -= discharge;
            entity.setChanged();
        }

        if (entity.storedEnergy > 0) {
            int sent = ElectricBlocks.trySend(Math.min(entity.storedEnergy, entity.getEnergyTransferRate()), (ServerLevel) level, pos, state.getValue(DirectionalMachineBlock.FACING));
            if (sent > 0) {
                entity.storedEnergy -= sent;
                entity.setChanged();
            }
        }

        entity.onTickEnd();
    }
}
