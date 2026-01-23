package io.github.reoseah.hayo.feature.energy_storages;

import io.github.reoseah.hayo.base.HayoContainerMenu;
import io.github.reoseah.hayo.feature.energy.item.EnergyComponents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class EnergyStorageMenu extends HayoContainerMenu {
    protected final ContainerData data;

    protected EnergyStorageMenu(MenuType<?> type, int menuId, Container container, ContainerData data, Inventory inventory) {
        super(type, menuId, container);

        this.data = data;
        this.addDataSlots(this.data);

        this.addSlot(new Slot(container, 0, 62, 18));
        this.addSlot(new Slot(container, 1, 62, 54));

        this.addStandardInventorySlots(inventory, 8, 84);
    }

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

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        var slot = this.slots.get(index);
        var stack = slot.getItem();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        var remaining = stack.copy();
        int firstPlayerSlot = 2;
        if (index < firstPlayerSlot) {
            if (!this.moveItemStackTo(stack, firstPlayerSlot, firstPlayerSlot + 36, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, remaining);
        } else {
            if (EnergyComponents.canDischargeInMachine(stack)) {
                if (!this.moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (EnergyComponents.canChargeInMachine(stack)) {
                if (!this.moveItemStackTo(stack, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < firstPlayerSlot + 27) {
                if (!this.moveItemStackTo(stack, firstPlayerSlot + 27, firstPlayerSlot + 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(stack, firstPlayerSlot, firstPlayerSlot + 27, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == remaining.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return remaining;
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
