package hayo.features.energy_storage;

import hayo.common.block.DirectionalElectricalBlock;
import hayo.common.blockentity.EnergyReceiverBlockEntity;
import hayo.energy.block.EnergyGrid;
import hayo.energy.item.EnergyComponents;
import lombok.Getter;
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

public abstract class EnergyStorageBlockEntity extends EnergyReceiverBlockEntity implements WorldlyContainer, MenuProvider {
    public static final int DISCHARGE_SLOT = 0, CHARGE_SLOT = 1, SLOTS = 2;

    @Getter
    protected int outputPerTick;
    @Getter
    protected float averageInput;
    @Getter
    protected float averageOutput;

    public EnergyStorageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, NonNullList.withSize(SLOTS, ItemStack.EMPTY));
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, EnergyStorageBlockEntity entity) {
        entity.chargeFromSlot(0);

        int transferred = 0;

        var chargeItem = entity.getItem(CHARGE_SLOT);
        if (!chargeItem.isEmpty()) {
            int limit = Math.min(entity.storedEnergy, entity.getEnergyTransferLimit() - transferred);
            int transfer = EnergyComponents.charge(limit, chargeItem);
            if (transfer > 0) {
                entity.storedEnergy -= transfer;
                transferred += transfer;
                entity.setChanged();
            }
        }

        if (entity.storedEnergy > 0) {
            int limit = Math.min(entity.storedEnergy, entity.getEnergyTransferLimit() - transferred);
            int transfer = EnergyGrid.trySend(limit, (ServerLevel) level, pos, state.getValue(DirectionalElectricalBlock.FACING));
            if (transfer > 0) {
                entity.storedEnergy -= transfer;
                transferred += transfer;
                entity.setChanged();
            }
        }

        entity.averageInput = Mth.lerp(0.05F, entity.averageInput, entity.inputPerTick);
        entity.averageOutput = Mth.lerp(0.05F, entity.averageOutput, transferred);
        entity.resetEnergyPerTick();
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
