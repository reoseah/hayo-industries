package io.github.reoseah.hayo.feature.energy_storages.crystal;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.energy_storages.EnergyStorageBlockEntity;
import io.github.reoseah.hayo.feature.energy_storages.EnergyStorageMenu;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

public class CrystalEnergyStorageMenu extends EnergyStorageMenu {
    public CrystalEnergyStorageMenu(int menuId, Inventory inventory) {
        this(menuId, new SimpleContainer(EnergyStorageBlockEntity.SLOTS), new SimpleContainerData(DATA_SLOTS), inventory);
    }

    public CrystalEnergyStorageMenu(int menuId, EnergyStorageBlockEntity entity, Inventory inventory) {
        this(menuId, entity, createData(entity), inventory);
    }

    protected CrystalEnergyStorageMenu(int menuId, Container container, ContainerData data, Inventory inventory) {
        super(Hayo.MenuTypes.CRYSTAL_ENERGY_STORAGE, menuId, container, data, inventory);
    }

    @Override
    public int getEnergyCapacity() {
        return CrystalEnergyStorageBlockEntity.CAPACITY;
    }
}
