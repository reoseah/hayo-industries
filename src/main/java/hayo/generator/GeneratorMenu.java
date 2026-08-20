package hayo.generator;

import hayo.Hayo;
import hayo.common.menu.HayoContainerMenu;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class GeneratorMenu extends HayoContainerMenu {
    public final GeneratorContainerData generatorData;

    public GeneratorMenu(int containerId, Inventory inventory) {
        this(containerId, new SimpleContainer(1), new GeneratorContainerData.Clientside(), inventory);
    }

    public GeneratorMenu(int containerId, GeneratorBlockEntity entity, Inventory inventory) {
        this(containerId, entity, new GeneratorContainerData.Serverside(entity), inventory);
    }

    protected GeneratorMenu(int containerId, Container container, GeneratorContainerData generatorData, Inventory inventory) {
        super(Hayo.MenuTypes.GENERATOR, containerId, container);

        this.addSlot(new GeneratorFuelSlot(container, 0, 62, 54, inventory.player.level().fuelValues()));
        this.addStandardInventorySlots(inventory, 8, 84);

        this.generatorData = generatorData;
        this.addDataSlots(this.generatorData);
    }

    @Override
    protected boolean handleQuickMoveFromInventory(ItemStack stack, Player player, int index) {
        if (player.level().fuelValues().isFuel(stack) && !stack.is(Hayo.ItemTags.DISABLED_GENERATOR_FUELS)) {
            return this.moveItemStackTo(stack, 0, 1, false);
        }
        return false;
    }
}
