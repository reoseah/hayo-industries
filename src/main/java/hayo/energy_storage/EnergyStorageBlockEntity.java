package hayo.energy_storage;

import hayo.common.block.DirectionalElectricalBlock;
import hayo.common.blockentity.SimpleElectricBlockEntity;
import hayo.energy.block.EnergyAPI;
import hayo.energy.item.EnergyComponents;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public abstract class EnergyStorageBlockEntity extends SimpleElectricBlockEntity implements WorldlyContainer, MenuProvider {
    public static final int DISCHARGE_SLOT = 0, CHARGE_SLOT = 1, SLOTS = 2;

    @Getter
    @Setter
    protected float averageInput;

    @Getter
    protected int outputPerTick;
    @Getter
    @Setter
    protected float averageOutput;

    public EnergyStorageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, NonNullList.withSize(SLOTS, ItemStack.EMPTY));
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
            int transfer = EnergyAPI.trySend(limit, (ServerLevel) level, pos, state.getValue(DirectionalElectricalBlock.FACING));
            if (transfer > 0) {
                entity.storedEnergy -= transfer;
                entity.outputPerTick += transfer;
                entity.setChanged();
            }
        }

        entity.resetEnergyPerTick();
    }

    @Override
    protected void resetEnergyPerTick() {
        this.averageInput = Mth.lerp(0.05F, this.averageInput, this.inputPerTick);
        super.resetEnergyPerTick();
        this.averageOutput = Mth.lerp(0.05F, this.averageOutput, this.outputPerTick);
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
        return slot == DISCHARGE_SLOT ? EnergyComponents.chargesBlocks(stack) : EnergyComponents.isStorage(stack);
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int menuId, Inventory playerInventory, Player player) {
        return new EnergyStorageMenu(menuId, this, playerInventory);
    }
}
