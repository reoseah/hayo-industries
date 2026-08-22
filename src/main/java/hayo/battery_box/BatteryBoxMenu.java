package hayo.battery_box;

import hayo.Hayo;
import hayo.common.menu.HayoContainerMenu;
import hayo.common.menuslot.BatteryBoxSlot;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class BatteryBoxMenu extends HayoContainerMenu {
    public final BatteryBoxContainerData batteryBoxData;

    public BatteryBoxMenu(int containerId, Inventory inventory) {
        this(containerId, new SimpleContainer(BatteryBoxBlockEntity.SLOTS), new BatteryBoxContainerData.Clientside(), inventory);
    }

    public BatteryBoxMenu(int containerId, BatteryBoxBlockEntity entity, Inventory inventory) {
        this(containerId, entity, new BatteryBoxContainerData.Serverside(entity), inventory);
    }

    protected BatteryBoxMenu(int containerId, Container container, BatteryBoxContainerData batteryBoxData, Inventory inventory) {
        super(Hayo.MenuTypes.BATTERY_BOX, containerId, container);

        this.batteryBoxData = batteryBoxData;
        this.addDataSlots(this.batteryBoxData);

        this.addSlot(new BatteryBoxSlot(container, 0, 26, 27));
        this.addSlot(new BatteryBoxSlot(container, 1, 44, 27));
        this.addSlot(new BatteryBoxSlot(container, 2, 62, 27));
        this.addSlot(new BatteryBoxSlot(container, 3, 26, 45));
        this.addSlot(new BatteryBoxSlot(container, 4, 44, 45));
        this.addSlot(new BatteryBoxSlot(container, 5, 62, 45));
        this.addSlot(new Slot(container, 6, 124, 36));

        this.addStandardInventorySlots(inventory, 8, 84);
    }
}
