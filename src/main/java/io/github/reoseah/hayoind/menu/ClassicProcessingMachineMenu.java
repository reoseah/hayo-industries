package io.github.reoseah.hayoind.menu;

import io.github.reoseah.hayoind.Hayo;
import io.github.reoseah.hayoind.block.entity.ElectricFurnaceBlockEntity;
import io.github.reoseah.hayoind.block.entity.MaceratorBlockEntity;
import io.github.reoseah.hayoind.block.entity.ProcessingMachineBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public abstract class ClassicProcessingMachineMenu extends AbstractContainerMenu {
    protected final ContainerData data;

    protected ClassicProcessingMachineMenu(MenuType<?> type, int menuId, Container container, ContainerData data, Inventory inventory) {
        super(type, menuId);

        this.data = data;
        this.addDataSlots(this.data);

        this.addSlot(new Slot(container, 0, 47, 18));
        this.addSlot(new Slot(container, 1, 47, 54));
        this.addSlot(new Slot(container, 2, 107, 36));

        this.addSlot(new Slot(container, 3, 152, 8));
        this.addSlot(new Slot(container, 4, 152, 26));
        this.addSlot(new Slot(container, 5, 152, 44));
        this.addSlot(new Slot(container, 6, 152, 62));

        this.addStandardInventorySlots(inventory, 8, 84);
    }

    public static ContainerData createData(ProcessingMachineBlockEntity<?, ?> entity) {
        return new ContainerData() {
            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> entity.getStoredEnergy() & 0xFFFF;
                    case 1 -> entity.getStoredEnergy() >>> 16;
                    case 2 -> entity.getRecipeUsedEnergy();
                    case 3 -> entity.getRecipeTotalEnergy();
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
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public int getStoredEnergy() {
        return (this.data.get(1) << 16) | (this.data.get(0) & 0xFFFF);
    }

    public int getRecipeUsedEnergy() {
        return this.data.get(2);
    }

    public int getRecipeTotalEnergy() {
        return this.data.get(3);
    }

    public static class ElectricFurnaceMenu extends ClassicProcessingMachineMenu {
        public ElectricFurnaceMenu(int menuId, Inventory inventory) {
            super(Hayo.MenuTypes.ELECTRIC_FURNACE, menuId, new SimpleContainer(7), new SimpleContainerData(4), inventory);
        }

        public ElectricFurnaceMenu(int menuId, ElectricFurnaceBlockEntity entity, Inventory inventory) {
            super(Hayo.MenuTypes.ELECTRIC_FURNACE, menuId, entity, createData(entity), inventory);
        }
    }

    public static class MaceratorMenu extends ClassicProcessingMachineMenu {
        public MaceratorMenu(int menuId, Inventory inventory) {
            super(Hayo.MenuTypes.MACERATOR, menuId, new SimpleContainer(7), new SimpleContainerData(4), inventory);
        }

        public MaceratorMenu(int menuId, MaceratorBlockEntity entity, Inventory inventory) {
            super(Hayo.MenuTypes.MACERATOR, menuId, entity, createData(entity), inventory);
        }
    }
}
