package io.github.reoseah.hayoind.menu;

import io.github.reoseah.hayoind.Hayo;
import io.github.reoseah.hayoind.block.entity.EnergyCrystalArrayBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class EnergyCrystalArrayMenu extends AbstractContainerMenu {
    protected final ContainerData data;

    public EnergyCrystalArrayMenu(int menuId, Inventory playerInventory) {
        this(menuId, new SimpleContainer(3), new SimpleContainerData(3), playerInventory);
    }

    public EnergyCrystalArrayMenu(int menuId, EnergyCrystalArrayBlockEntity entity, Inventory playerInventory) {
        this(menuId, entity, createData(entity), playerInventory);
    }

    protected static ContainerData createData(EnergyCrystalArrayBlockEntity entity) {
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

    protected EnergyCrystalArrayMenu(int menuId, Container container, ContainerData data, Inventory playerInventory) {
        super(Hayo.MenuTypes.ENERGY_CRYSTAL_ARRAY, menuId);

        this.data = data;
        this.addDataSlots(this.data);

        this.addSlot(new Slot(container, 0, 62, 18));
        this.addSlot(new Slot(container, 1, 62, 54));

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

    public int getStoredEnergy() {
        return (this.data.get(1) << 16) | (this.data.get(0) & 0xFFFF);
    }

    public float getAverageEnergyPerTick() {
        return this.data.get(2) / 10F;
    }
}
