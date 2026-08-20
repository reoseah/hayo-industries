package hayo.generator;

import hayo.Hayo;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FuelValues;

public class GeneratorFuelSlot extends Slot {
    protected final FuelValues fuelValues;

    public GeneratorFuelSlot(Container container, int slot, int x, int y, FuelValues fuelValues) {
        super(container, slot, x, y);
        this.fuelValues = fuelValues;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.fuelValues.isFuel(stack) && !stack.is(Hayo.ItemTags.DISABLED_GENERATOR_FUELS);
    }
}
