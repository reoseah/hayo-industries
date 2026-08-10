package io.github.reoseah.hayo.feature.energy_storages;

import io.github.reoseah.hayo.base.block.DirectionalElectricalBlock;
import io.github.reoseah.hayo.feature.electric_blocks.ElectricBlockManager;
import io.github.reoseah.hayo.feature.electric_blocks.SimpleElectricBlockEntity;
import io.github.reoseah.hayo.feature.electric_items.EnergyComponents;
import io.github.reoseah.hayo.feature.universal_screen.SpriteElement;
import io.github.reoseah.hayo.feature.universal_screen.StorageEnergyBar;
import io.github.reoseah.hayo.feature.universal_screen.UniversalContainerMenu;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public abstract class EnergyStorageBlockEntity extends SimpleElectricBlockEntity implements WorldlyContainer, ExtendedMenuProvider<BlockPos> {
    public static final int DISCHARGE_SLOT = 0, CHARGE_SLOT = 1, SLOTS = 2;

    @Getter
    @Setter
    protected float averageInputPerTick;
    @Getter
    protected int outputPerTick;
    @Getter
    @Setter
    protected float averageOutputPerTick;

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
            int transfer = ElectricBlockManager.trySend(limit, (ServerLevel) level, pos, state.getValue(DirectionalElectricalBlock.FACING));
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

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.worldPosition;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int menuId, Inventory playerInventory, Player player) {
        return new UniversalContainerMenu(menuId, this).setCenterTitle(true) //
                .addDataSlotsChainable(new EnergyStorageData(this)) //
                .addSlotChainable(new Slot(this, DISCHARGE_SLOT, 62, 18)) //
                .addSlotChainable(new Slot(this, CHARGE_SLOT, 62, 54)) //
                .addStandardInventorySlotsChainable(playerInventory) //
                .addQuickMoveRule(DISCHARGE_SLOT, DISCHARGE_SLOT + 1, EnergyComponents::canDischargeInMachine) //
                .addQuickMoveRule(CHARGE_SLOT, CHARGE_SLOT + 1, EnergyComponents::canChargeInMachine) //
                .addElement(new StorageEnergyBar(88, 16, this::getStoredEnergy, this::getEnergyCapacity, this::getAverageInputPerTick, this::getAverageOutputPerTick)) //
                .addElement(SpriteElement.smallArrowRight(79, 17)) //
                .addElement(SpriteElement.smallArrowLeft(79, 53));
    }

    public record EnergyStorageData(EnergyStorageBlockEntity entity) implements ContainerData {
        private static final int DATA_SLOTS = 4;

        @Override
        public int getCount() {
            return DATA_SLOTS;
        }

        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> this.entity.storedEnergy & 0xFFFF;
                case 1 -> this.entity.storedEnergy >>> 16;
                case 2 -> Math.round(this.entity.averageInputPerTick * 10);
                case 3 -> Math.round(this.entity.averageOutputPerTick * 10);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            value &= 0xFFFF;
            switch (index) {
                case 0 -> this.entity.storedEnergy = this.entity.storedEnergy & 0xFFFF_0000 | value;
                case 1 -> this.entity.storedEnergy = this.entity.storedEnergy & 0xFFFF | value << 16;
                case 2 -> this.entity.averageInputPerTick = value / 10F;
                case 3 -> this.entity.averageOutputPerTick = value / 10F;
            }
        }
    }
}
