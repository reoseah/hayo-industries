package hayo.features.geothermal_generator;

import hayo.Hayo;
import hayo.common.menu.HayoContainerMenu;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GeothermalGeneratorMenu extends HayoContainerMenu {
    public final GeothermalGeneratorData generatorData;

    public GeothermalGeneratorMenu(int containerId, Inventory inventory) {
        this(containerId, new SimpleContainer(GeothermalGeneratorBlockEntity.SLOTS), new GeothermalGeneratorData.Clientside(), inventory);
    }

    public GeothermalGeneratorMenu(int containerId, GeothermalGeneratorBlockEntity entity, Inventory inventory) {
        this(containerId, entity, new GeothermalGeneratorData.Serverside(entity), inventory);
    }

    protected GeothermalGeneratorMenu(int containerId, Container container, GeothermalGeneratorData generatorData, Inventory inventory) {
        super(Hayo.MenuTypes.GEOTHERMAL_GENERATOR, containerId, container);

        this.addSlot(new Slot(container, GeothermalGeneratorBlockEntity.INPUT, 27, 17));
        this.addSlot(new Slot(container, GeothermalGeneratorBlockEntity.OUTPUT, 27, 53));
        this.addStandardInventorySlots(inventory, 8, 84);

        this.generatorData = generatorData;
        this.addDataSlots(this.generatorData);
    }

    @Override
    protected boolean handleQuickMoveFromInventory(ItemStack stack, Player player, int index) {
//        if (player.level().fuelValues().isFuel(stack) && !stack.is(Hayo.ItemTags.DISABLED_GENERATOR_FUELS)) {
//            return this.moveItemStackTo(stack, 0, 1, false);
//        }
        return false;
    }
}
