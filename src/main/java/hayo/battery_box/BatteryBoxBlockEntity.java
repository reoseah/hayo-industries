package hayo.battery_box;

import hayo.Hayo;
import hayo.common.block.HorizontalDirectionalElectricalBlock;
import hayo.common.blockentity.EnergyReceiverBlockEntity;
import hayo.energy.block.EnergyGrid;
import hayo.energy.item.EnergyComponents;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class BatteryBoxBlockEntity extends EnergyReceiverBlockEntity implements WorldlyContainer, MenuProvider {
    public static final int BATTERIES = 6, CHARGING_SLOT = 6, SLOTS = 7;

    @Getter
    @Setter
    protected float averageInput;
    @Getter
    protected int outputPerTick;
    @Getter
    @Setter
    protected float averageOutput;

    protected int capacity = 0;
    protected int transferLimit = 0;
    protected int batteryCount = 0;
    protected boolean batteryCountChanged = false;

    public BatteryBoxBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.BATTERY_BOX, pos, state, NonNullList.withSize(SLOTS, ItemStack.EMPTY));
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, BatteryBoxBlockEntity entity) {
        if (entity.batteryCountChanged) {
            if (entity.batteryCount != state.getValue(BatteryBoxBlock.BATTERIES)) {
                level.setBlockAndUpdate(pos, state.setValue(BatteryBoxBlock.BATTERIES, entity.batteryCount));
            }
            entity.batteryCountChanged = false;
        }

        var chargeItem = entity.getItem(CHARGING_SLOT);
        if (!chargeItem.isEmpty()) {
            int limit = Math.min(entity.storedEnergy, entity.getEnergyTransferLimit() - entity.outputPerTick);
            int transfer = EnergyComponents.charge(limit, chargeItem);
            if (transfer > 0) {
                entity.extractFromBatteries(transfer);
                entity.storedEnergy -= transfer;
                entity.outputPerTick += transfer;
                entity.setChanged();
            }
        }

        int limit = Math.min(entity.storedEnergy, entity.getEnergyTransferLimit() - entity.outputPerTick);
        if (limit > 0) {
            int transfer = EnergyGrid.trySend(limit, (ServerLevel) level, pos, state.getValue(HorizontalDirectionalElectricalBlock.FACING));
            if (transfer > 0) {
                entity.extractFromBatteries(transfer);
                entity.storedEnergy -= transfer;
                entity.outputPerTick += transfer;
                entity.setChanged();
            }
        }

        entity.resetEnergyPerTick();
    }

    @Override
    public int getEnergyCapacity() {
        return this.capacity;
    }

    @Override
    protected int getEnergyTransferLimit() {
        return this.transferLimit;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.discard("stored_energy");
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.storedEnergy = 0;
        this.updateEnergyStats();
    }

    @Override
    protected void inventoryChanged(int slot, ItemStack previous, ItemStack stack) {
        super.inventoryChanged(slot, previous, stack);
        if (slot != CHARGING_SLOT) {
            this.updateEnergyStats();
        }
    }

    protected void updateEnergyStats() {
        int batteryCount = 0;
        int capacity = 0;
        int transferLimit = 0;
        int energy = 0;
        for (int i = 0; i < BATTERIES; i++) {
            var item = this.getItem(i);
            var itemStorage = item.get(EnergyComponents.STORES_ENERGY);
            if (itemStorage == null) {
                continue;
            }
            batteryCount++;
            capacity += itemStorage.capacity();
            transferLimit += itemStorage.transferLimit();
            energy += EnergyComponents.getEnergy(item);
        }
        this.capacity = capacity;
        this.transferLimit = Math.min(transferLimit, 32);
        this.storedEnergy = energy;
        if (this.batteryCount != batteryCount) {
            this.batteryCount = batteryCount;
            this.batteryCountChanged = true;
        }
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.hayo.battery_box");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new BatteryBoxMenu(containerId, this, inventory);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[]{CHARGING_SLOT};
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == CHARGING_SLOT && !EnergyComponents.isStorage(stack);
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return slot == CHARGING_SLOT && EnergyComponents.isStorage(stack);
    }

    @Override
    protected void resetEnergyPerTick() {
        this.averageInput = Mth.lerp(0.05F, this.averageInput, this.inputPerTick);
        super.resetEnergyPerTick();
        this.averageOutput = Mth.lerp(0.05F, this.averageOutput, this.outputPerTick);
        this.outputPerTick = 0;
    }

    @Override
    public int receiveEnergy(int amount) {
        int transfer = super.receiveEnergy(amount);
        this.insertToBatteries(transfer);
        return transfer;
    }

    protected void insertToBatteries(int amount) {
        int leftToInsert = amount;
        for (int i = 0; i < BATTERIES; i++) {
            var item = this.getItem(i);
            var itemStorage = item.get(EnergyComponents.STORES_ENERGY);
            if (itemStorage == null) {
                continue;
            }
            int itemEnergy = EnergyComponents.getEnergy(item);
            int vacancy = itemStorage.capacity() - itemEnergy;
            if (vacancy > 0) {
                int inserted = Math.min(leftToInsert, Math.min(vacancy, itemStorage.transferLimit()));
                EnergyComponents.setEnergy(item, itemEnergy + inserted);
                leftToInsert -= inserted;

                if (leftToInsert == 0) {
                    break;
                }
            }
        }
    }

    protected void extractFromBatteries(int amount) {
        int leftToExtract = amount;
        for (int i = BATTERIES - 1; i >= 0; i--) {
            var item = this.getItem(i);
            var itemStorage = item.get(EnergyComponents.STORES_ENERGY);
            if (itemStorage == null) {
                continue;
            }
            int itemEnergy = EnergyComponents.getEnergy(item);
            if (itemEnergy > 0) {
                int extracted = Math.min(leftToExtract, Math.min(itemEnergy, itemStorage.transferLimit()));
                EnergyComponents.setEnergy(item, itemEnergy - extracted);
                leftToExtract -= extracted;

                if (leftToExtract == 0) {
                    break;
                }
            }
        }
    }
}
