package hayo.energy_storage;

import hayo.Hayo;
import hayo.common.menu.HayoContainerMenu;
import hayo.energy.item.EnergyComponents;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class EnergyStorageMenu extends HayoContainerMenu {
    public final EnergyStorageContainerData storageData;

    public EnergyStorageMenu(int containerId, Inventory playerInventory) {
        this(containerId, new SimpleContainer(EnergyStorageBlockEntity.SLOTS), new EnergyStorageContainerData.Clientside(), playerInventory);
    }

    public EnergyStorageMenu(int containerId, EnergyStorageBlockEntity entity, Inventory playerInventory) {
        this(containerId, entity, new EnergyStorageContainerData.Serverside(entity), playerInventory);
    }

    protected EnergyStorageMenu(int containerId, Container container, EnergyStorageContainerData storageData, Inventory playerInventory) {
        super(Hayo.MenuTypes.ENERGY_STORAGE, containerId, container);

        this.storageData = storageData;
        this.addDataSlots(this.storageData);

        this.addSlot(new Slot(container, 0, 62, 18));
        this.addSlot(new Slot(container, 1, 62, 54));
        this.addStandardInventorySlots(playerInventory, 8, 84);
    }

    @Override
    protected boolean handleQuickMoveFromInventory(ItemStack stack, Player player, int index) {
        if (EnergyComponents.canChargeMachine(stack) && this.moveItemStackTo(stack, 0, 1, false)) {
            return true;
        }
        if (EnergyComponents.isStorage(stack)) {
            return this.moveItemStackTo(stack, 1, 2, false);
        }
        return false;
    }
}
