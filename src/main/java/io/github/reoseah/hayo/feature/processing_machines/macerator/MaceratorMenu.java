package io.github.reoseah.hayo.feature.processing_machines.macerator;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.processing_machines.MachineBlockEntity;
import io.github.reoseah.hayo.feature.processing_machines.MachineMenu;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class MaceratorMenu extends MachineMenu {
    public MaceratorMenu(int menuId, Inventory inventory) {
        this(menuId, new SimpleContainer(MaceratorBlockEntity.SLOTS), new SimpleContainerData(7), inventory);
    }

    public MaceratorMenu(int menuId, MaceratorBlockEntity entity, Inventory inventory) {
        this(menuId, entity, MachineBlockEntity.createData(entity), inventory);
    }

    protected MaceratorMenu(int menuId, Container container, ContainerData data, Inventory inventory) {
        super(Hayo.MenuTypes.MACERATOR, menuId, container, data, inventory);

        addClassicSlots(this, container, inventory, Hayo.ItemTags.MACERATOR_UPGRADES);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveClassicMachineStack(this, player, index, 1, 1, 1, 4, this::isRecipeInput, Hayo.ItemTags.MACERATOR_UPGRADES);
    }

    protected boolean isRecipeInput(ItemStack stack) {
        // TODO: synchronize recipe inputs, quick move only valid inputs
        return true;
    }

    @Override
    public int getEnergyUseRate() {
        return MaceratorBlockEntity.ENERGY_USE_RATE * (1 + this.getOverclockCount());
    }
}
