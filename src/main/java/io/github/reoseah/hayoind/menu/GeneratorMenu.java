package io.github.reoseah.hayoind.menu;

import io.github.reoseah.hayoind.Hayo;
import io.github.reoseah.hayoind.block.entity.GeneratorBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GeneratorMenu extends AbstractContainerMenu {
    protected final ContainerData data;

    public GeneratorMenu(int menuId, Inventory playerInventory) {
        this(menuId, new SimpleContainer(1), new SimpleContainerData(4), playerInventory);
    }

    public GeneratorMenu(int menuId, GeneratorBlockEntity generator, Inventory playerInventory) {
        this(menuId, generator, createData(generator), playerInventory);
    }

    protected static ContainerData createData(GeneratorBlockEntity entity) {
        return new ContainerData() {
            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> entity.getFuelEnergyLeft();
                    case 1 -> entity.getFuelEnergyTotal();
                    case 2 -> entity.getStoredEnergy() & 0xFFFF;
                    case 3 -> entity.getStoredEnergy() >>> 16;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }
        };
    }

    protected GeneratorMenu(int menuId, Container generator, ContainerData data, Inventory playerInventory) {
        super(Hayo.MenuTypes.GENERATOR, menuId);

        this.data = data;
        this.addDataSlots(this.data);

        this.addSlot(new Slot(generator, 0, 62, 54));
        this.addStandardInventorySlots(playerInventory, 8, 84);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public int getFuelEnergyLeft() {
        return this.data.get(0);
    }

    public int getFuelEnergyTotal() {
        return this.data.get(1);
    }

    public int getStoredEnergy() {
        return (this.data.get(3) << 16) | (this.data.get(2) & 0xFFFF);
    }
}
