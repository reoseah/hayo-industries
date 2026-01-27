package io.github.reoseah.hayo.feature.energy_storages.advanced;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.energy_storages.EnergyStorageBlockEntity;
import io.github.reoseah.hayo.feature.energy_storages.EnergyStorageMenu;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

public class AdvancedEnergyStorageMenu extends EnergyStorageMenu {
    public AdvancedEnergyStorageMenu(int menuId, Inventory inventory) {
        this(menuId, new SimpleContainer(EnergyStorageBlockEntity.SLOTS), new SimpleContainerData(DATA_SLOTS), inventory);
    }

    public AdvancedEnergyStorageMenu(int menuId, EnergyStorageBlockEntity entity, Inventory inventory) {
        this(menuId, entity, createData(entity), inventory);
    }

    protected AdvancedEnergyStorageMenu(int menuId, Container container, ContainerData data, Inventory inventory) {
        super(Hayo.MenuTypes.ADVANCED_ENERGY_STORAGE, menuId, container, data, inventory);
    }

    @Override
    public int getEnergyCapacity() {
        return AdvancedEnergyStorageBlockEntity.CAPACITY;
    }
}
