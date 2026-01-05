package io.github.reoseah.hayo.feature.energy_storages.battery_array;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.energy_storages.EnergyStorageBlockEntity;
import io.github.reoseah.hayo.feature.energy_storages.EnergyStorageMenu;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;

public class BatteryArrayMenu extends EnergyStorageMenu {
    public BatteryArrayMenu(int menuId, Inventory inventory) {
        this(menuId, new SimpleContainer(2), createData(), inventory);
    }

    public BatteryArrayMenu(int menuId, EnergyStorageBlockEntity entity, Inventory inventory) {
        this(menuId, entity, createData(entity), inventory);
    }

    protected BatteryArrayMenu(int menuId, Container container, ContainerData data, Inventory inventory) {
        super(Hayo.MenuTypes.BATTERY_ARRAY, menuId, container, data, inventory);
    }

    @Override
    public int getEnergyCapacity() {
        return BatteryArrayBlockEntity.CAPACITY;
    }
}
