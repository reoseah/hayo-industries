package hayo.features.geothermal_generator;

import hayo.Hayo;
import hayo.common.menu.HayoContainerMenu;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.material.Fluids;

public class GeothermalGeneratorMenu extends HayoContainerMenu {
    public final GeothermalGeneratorData generatorData;

    public GeothermalGeneratorMenu(int containerId, Inventory inventory) {
        this(containerId, new SimpleContainer(2), new GeothermalGeneratorData.Clientside(), inventory);
    }

    public GeothermalGeneratorMenu(int containerId, GeothermalGeneratorBlockEntity entity, Inventory inventory) {
        this(containerId, entity, new GeothermalGeneratorData.Serverside(entity), inventory);
    }

    protected GeothermalGeneratorMenu(int containerId, Container container, GeothermalGeneratorData generatorData, Inventory inventory) {
        super(Hayo.MenuTypes.GEOTHERMAL_GENERATOR, containerId, container);

        this.addSlot(new Slot(container, 0, 45, 17));
        this.addSlot(new Slot(container, 1, 45, 53));
        this.addStandardInventorySlots(inventory, 8, 84);

        this.generatorData = generatorData;
        this.addDataSlots(this.generatorData);
    }

    @Override
    protected boolean handleQuickMoveFromInventory(ItemStack stack, Player player, int index) {
        var recipeHolder = player.level()
                .recipeAccess()
                .getSynchronizedRecipes()
                .getFirstMatch(Hayo.RecipeTypes.FLUID_DRAINING, new SingleRecipeInput(stack), player.level())
                .orElse(null);
        if (recipeHolder != null && recipeHolder.value().resultFluid().fluid() == Fluids.LAVA) {
            return this.moveItemStackTo(stack, 0, 1, false);
        }
        return false;
    }
}
