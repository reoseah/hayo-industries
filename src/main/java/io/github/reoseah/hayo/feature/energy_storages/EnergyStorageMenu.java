package io.github.reoseah.hayo.feature.energy_storages;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public abstract class EnergyStorageMenu extends AbstractContainerMenu {
    protected final ContainerData data;

    protected static ContainerData createData() {
        return new SimpleContainerData(3);
    }

    protected static ContainerData createData(EnergyStorageBlockEntity entity) {
        return new ContainerData() {
            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> entity.getStoredEnergy() & 0xFFFF;
                    case 1 -> entity.getStoredEnergy() >>> 16;
                    case 2 -> Math.round(entity.getAverageEnergyPerTick() * 10);
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }
        };
    }

    protected EnergyStorageMenu(MenuType<?> type, int menuId, Container container, ContainerData data, Inventory inventory) {
        super(type, menuId);

        this.data = data;
        this.addDataSlots(this.data);

        this.addSlot(new Slot(container, 0, 62, 18));
        this.addSlot(new Slot(container, 1, 62, 54));

        this.addStandardInventorySlots(inventory, 8, 84);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // TODO
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public int getStoredEnergy() {
        return (this.data.get(1) << 16) | (this.data.get(0) & 0xFFFF);
    }

    public float getAverageEnergyPerTick() {
        return this.data.get(2) / 10F;
    }

    public abstract int getEnergyCapacity();
}
