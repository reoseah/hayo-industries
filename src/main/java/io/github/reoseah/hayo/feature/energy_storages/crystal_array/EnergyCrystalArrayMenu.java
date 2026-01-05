package io.github.reoseah.hayo.feature.energy_storages.crystal_array;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.energy_storages.EnergyStorageBlockEntity;
import io.github.reoseah.hayo.feature.energy_storages.EnergyStorageMenu;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;

public class EnergyCrystalArrayMenu extends EnergyStorageMenu {
    public EnergyCrystalArrayMenu(int menuId, Inventory inventory) {
        this(menuId, new SimpleContainer(2), createData(), inventory);
    }

    public EnergyCrystalArrayMenu(int menuId, EnergyStorageBlockEntity entity, Inventory inventory) {
        this(menuId, entity, createData(entity), inventory);
    }

    protected EnergyCrystalArrayMenu(int menuId, Container container, ContainerData data, Inventory inventory) {
        super(Hayo.MenuTypes.ENERGY_CRYSTAL_ARRAY, menuId, container, data, inventory);
    }

    @Override
    public int getEnergyCapacity() {
        return EnergyCrystalArrayBlockEntity.CAPACITY;
    }
}
