package io.github.reoseah.hayo.feature.energy_storages;

import io.github.reoseah.hayo.base.block.DirectionalMachineBlock;
import io.github.reoseah.hayo.base.block.entity.ElectricBlockEntity;
import io.github.reoseah.hayo.feature.electric_blocks.ElectricBlocks;
import io.github.reoseah.hayo.feature.electric_items.EnergyComponents;
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
    public static final int DISCHARGE_SLOT = 0, CHARGE_SLOT = 1, SLOTS = 2;

    @Getter
    protected float averageInputPerTick;
    @Getter
    protected int outputPerTick;
    @Getter
    protected float averageOutputPerTick;

    public EnergyStorageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, EnergyStorageBlockEntity entity) {
        entity.chargeFromSlot(0);

        var chargeItem = entity.getItem(CHARGE_SLOT);
        if (!chargeItem.isEmpty()) {
            int limit = Math.min(entity.storedEnergy, entity.getEnergyTransferLimit() - entity.outputPerTick);
            int transfer = EnergyComponents.charge(limit, chargeItem);
            if (transfer > 0) {
                entity.storedEnergy -= transfer;
                entity.outputPerTick += transfer;
                entity.setChanged();
            }
        }

        if (entity.storedEnergy > 0) {
            int limit = Math.min(entity.storedEnergy, entity.getEnergyTransferLimit() - entity.outputPerTick);
            int transfer = ElectricBlocks.trySend(limit, (ServerLevel) level, pos, state.getValue(DirectionalMachineBlock.FACING));
            if (transfer > 0) {
                entity.storedEnergy -= transfer;
                entity.outputPerTick += transfer;
                entity.setChanged();
            }
        }

        entity.resetEnergyPerTick();
    }

    @Override
    protected NonNullList<ItemStack> createInventory() {
        return NonNullList.withSize(SLOTS, ItemStack.EMPTY);
    }

    @Override
    protected void resetEnergyPerTick() {
        this.averageInputPerTick = Mth.lerp(0.05F, this.averageInputPerTick, this.inputPerTick);
        super.resetEnergyPerTick();
        this.averageOutputPerTick = Mth.lerp(0.05F, this.averageOutputPerTick, this.outputPerTick);
        this.outputPerTick = 0;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return switch (direction) {
            case UP -> new int[]{DISCHARGE_SLOT};
            case DOWN -> new int[]{DISCHARGE_SLOT, CHARGE_SLOT};
            default -> new int[]{CHARGE_SLOT};
        };
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return !this.canPlaceItemThroughFace(slot, stack, direction);
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return slot == DISCHARGE_SLOT ? EnergyComponents.canDischargeInMachine(stack) : EnergyComponents.canChargeInMachine(stack);
    }
}
