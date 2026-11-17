package hayo.features.generator;

import hayo.Hayo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GeneratorFuelSlot extends Slot {
    public GeneratorFuelSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.has(DataComponents.COOKING_FUEL) && !stack.is(Hayo.ItemTags.DISABLED_GENERATOR_FUELS);
    }
}
