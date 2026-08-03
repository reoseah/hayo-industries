package io.github.reoseah.hayo.feature.battery_box;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.base.block.OrientableMachineBlock;
import io.github.reoseah.hayo.feature.electric_blocks.ElectricBlockManager;
import io.github.reoseah.hayo.feature.electric_blocks.SimpleElectricBlockEntity;
import io.github.reoseah.hayo.feature.electric_items.EnergyComponents;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import org.jspecify.annotations.Nullable;

public class BatteryBoxBlockEntity extends SimpleElectricBlockEntity implements WorldlyContainer {
    public static final int BATTERY_SLOTS = 6, CHARGING_SLOT = 6, SLOTS = 7;

    @Getter
    protected float averageInputPerTick;
    @Getter
    protected int outputPerTick;
    @Getter
    protected float averageOutputPerTick;

    protected int capacity = 0;
    protected int transferLimit = 0;
    protected int batteryCount = 0;
    protected boolean batteryCountChanged = false;

    public BatteryBoxBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.BATTERY_BOX, pos, state);
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

        if (entity.storedEnergy > 0) {
            int limit = Math.min(entity.storedEnergy, entity.getEnergyTransferLimit() - entity.outputPerTick);
            int transfer = ElectricBlockManager.trySend(limit, (ServerLevel) level, pos, state.getValue(OrientableMachineBlock.FACING));
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
    protected NonNullList<ItemStack> createInventory() {
        return NonNullList.withSize(SLOTS, ItemStack.EMPTY);
    }

    @Override
    protected int getEnergyCapacity() {
        return this.capacity;
    }

    @Override
    protected int getEnergyTransferLimit() {
        return this.transferLimit;
    }

    @Override
    protected boolean doesStoredEnergyPersist() {
        return false;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.updateEnergyStats();
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
        this.updateEnergyStats();
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        var item = super.removeItem(slot, amount);
        this.updateEnergyStats();
        return item;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        var item = super.removeItemNoUpdate(slot);
        this.updateEnergyStats();
        return item;
    }

    protected void updateEnergyStats() {
        int batteryCount = 0;
        int capacity = 0;
        int transferLimit = 0;
        int energy = 0;
        for (int i = 0; i < BATTERY_SLOTS; i++) {
            var item = this.getItem(i);
            var itemStorage = item.get(EnergyComponents.ENERGY_STORAGE);
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
        return slot == CHARGING_SLOT && !EnergyComponents.canChargeInMachine(stack);
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return slot == CHARGING_SLOT && EnergyComponents.canChargeInMachine(stack);
    }

    @Override
    protected void resetEnergyPerTick() {
        this.averageInputPerTick = Mth.lerp(0.05F, this.averageInputPerTick, this.inputPerTick);
        super.resetEnergyPerTick();
        this.averageOutputPerTick = Mth.lerp(0.05F, this.averageOutputPerTick, this.outputPerTick);
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
        for (int i = 0; i < BATTERY_SLOTS; i++) {
            var item = this.getItem(i);
            var itemStorage = item.get(EnergyComponents.ENERGY_STORAGE);
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
        for (int i = BATTERY_SLOTS - 1; i >= 0; i--) {
            var item = this.getItem(i);
            var itemStorage = item.get(EnergyComponents.ENERGY_STORAGE);
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
