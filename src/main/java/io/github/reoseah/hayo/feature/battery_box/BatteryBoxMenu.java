package io.github.reoseah.hayo.feature.battery_box;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.electric_blocks.EnergyGuiSprites;
import io.github.reoseah.hayo.feature.electric_items.EnergyComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BatteryBoxMenu extends AbstractContainerMenu {
    public static final int DATA_SLOTS = 8;

    protected final Container container;
    protected final ContainerData data;

    protected BatteryBoxMenu(int containerId, Container container, ContainerData data, Inventory inventory) {
        super(Hayo.MenuTypes.BATTERY_BOX, containerId);

        this.container = container;

        this.data = data;
        this.addDataSlots(this.data);

        this.addSlot(new BatterySlot(container, 0, 26, 27));
        this.addSlot(new BatterySlot(container, 1, 44, 27));
        this.addSlot(new BatterySlot(container, 2, 62, 27));
        this.addSlot(new BatterySlot(container, 3, 26, 45));
        this.addSlot(new BatterySlot(container, 4, 44, 45));
        this.addSlot(new BatterySlot(container, 5, 62, 45));
        this.addSlot(new Slot(container, 6, 124, 36));

        this.addStandardInventorySlots(inventory, 8, 84);
    }

    public BatteryBoxMenu(int containerId, Inventory inventory) {
        this(containerId, new SimpleContainer(BatteryBoxBlockEntity.SLOTS), new SimpleContainerData(DATA_SLOTS), inventory);
    }

    public BatteryBoxMenu(int containerId, BatteryBoxBlockEntity entity, Inventory inventory) {
        this(containerId, entity, createData(entity), inventory);
    }

    protected static ContainerData createData(BatteryBoxBlockEntity entity) {
        return new ContainerData() {
            @Override
            public int getCount() {
                return DATA_SLOTS;
            }

            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> entity.getStoredEnergy() & 0xFFFF;
                    case 1 -> entity.getStoredEnergy() >>> 16;
                    case 2 -> entity.getEnergyCapacity() & 0xFFFF;
                    case 3 -> entity.getEnergyCapacity() >>> 16;
                    case 4 -> entity.getEnergyTransferLimit() & 0xFFFF;
                    case 5 -> entity.getEnergyTransferLimit() >>> 16;
                    case 6 -> Math.round(entity.getAverageInputPerTick() * 10);
                    case 7 -> Math.round(entity.getAverageOutputPerTick() * 10);
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }
        };
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        var slot = this.slots.get(index);
        var stack = slot.getItem();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        var remaining = stack.copy();
        int firstPlayerSlot = this.container.getContainerSize();
        if (index < firstPlayerSlot) {
            if (!this.moveItemStackTo(stack, firstPlayerSlot, firstPlayerSlot + 36, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, remaining);
        } else {
            if (stack.is(Hayo.ItemTags.BATTERY_BOX_BATTERIES)) {
                if (!this.moveItemStackTo(stack, 0, 6, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (EnergyComponents.canChargeInMachine(stack)) {
                if (!this.moveItemStackTo(stack, 6, 7, false)) {
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

    public int getStoredEnergy() {
        return (this.data.get(1) << 16) | (this.data.get(0) & 0xFFFF);
    }

    public int getEnergyCapacity() {
        return (this.data.get(3) << 16) | (this.data.get(2) & 0xFFFF);
    }

    public int getEnergyTransferLimit() {
        return (this.data.get(5) << 16) | (this.data.get(4) & 0xFFFF);
    }

    public float getAverageInput() {
        return this.data.get(6) / 10F;
    }

    public float getAverageOutput() {
        return this.data.get(7) / 10F;
    }

    private static class BatterySlot extends Slot {
        public BatterySlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(Hayo.ItemTags.BATTERY_BOX_BATTERIES);
        }

        @Override
        public Identifier getNoItemIcon() {
            return EnergyGuiSprites.BATTERY_SLOT_ICON;
        }
    }
}
