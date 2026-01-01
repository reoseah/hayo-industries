package io.github.reoseah.hayo.feature.generator;

import io.github.reoseah.hayo.Hayo;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GeneratorMenu extends AbstractContainerMenu {
    protected final Level level;
    protected final ContainerData data;

    public GeneratorMenu(int menuId, Inventory inventory) {
        this(menuId, new SimpleContainer(1), new SimpleContainerData(6), inventory);
    }

    public GeneratorMenu(int menuId, GeneratorBlockEntity entity, Inventory inventory) {
        this(menuId, entity, createData(entity), inventory);
    }

    protected static ContainerData createData(GeneratorBlockEntity entity) {
        return new ContainerData() {
            @Override
            public int getCount() {
                return 6;
            }

            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> entity.getStoredEnergy() & 0xFFFF;
                    case 1 -> entity.getStoredEnergy() >>> 16;
                    case 2 -> entity.getFuelEnergyLeft() & 0xFFFF;
                    case 3 -> entity.getFuelEnergyLeft() >>> 16;
                    case 4 -> entity.getFuelEnergyTotal() & 0xFFFF;
                    case 5 -> entity.getFuelEnergyTotal() >>> 16;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }
        };
    }

    protected GeneratorMenu(int menuId, Container container, ContainerData data, Inventory inventory) {
        super(Hayo.MenuTypes.GENERATOR, menuId);

        this.level = inventory.player.level();

        this.data = data;
        this.addDataSlots(this.data);

        this.addSlot(new Slot(container, 0, 62, 54));
        this.addStandardInventorySlots(inventory, 8, 84);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        var slot = this.slots.get(index);
        var stack = slot.getItem();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        var remaining = stack.copy();
        int firstPlayerSlot = 1;
        if (index < firstPlayerSlot) {
            if (!this.moveItemStackTo(stack, firstPlayerSlot, firstPlayerSlot + 36, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, remaining);
        } else {
            if (this.level.fuelValues().isFuel(stack)) {
                if (!this.moveItemStackTo(stack, 0, 1, false)) {
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

    public int getFuelEnergyLeft() {
        return (this.data.get(3) << 16) | (this.data.get(2) & 0xFFFF);
    }

    public int getFuelEnergyTotal() {
        return (this.data.get(5) << 16) | (this.data.get(4) & 0xFFFF);
    }
}
