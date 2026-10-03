package hayo.common;

import hayo.features.processing_machine.solid_fluid_reactor.SolidFluidReactorBlockEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.List;

public class HayoContainerUtils {
    public static boolean canInsertAllShapelessly(NonNullList<ItemStack> inventory, List<ItemStackTemplate> templates, int from, int to, int maxStackSize) {
        if (templates.isEmpty()) {
            return true;
        }

        var simulated = NonNullList.withSize(SolidFluidReactorBlockEntity.SLOTS, ItemStack.EMPTY);
        for (int slot = from; slot <= to; slot++) {
            simulated.set(slot, inventory.get(slot).copy());
        }

        for (var template : templates) {
            var stack = template.create();
            if (!insertShapelessly(simulated, stack, from, to, maxStackSize)) {
                return false;
            }
        }

        return true;
    }

    public static boolean canInsertAllShapelessly(NonNullList<ItemStack> inventory, List<ItemStackTemplate> templates, List<Float> extraChances, int from, int to, int maxStackSize) {
        if (templates.isEmpty()) {
            return true;
        }

        var simulated = NonNullList.withSize(SolidFluidReactorBlockEntity.SLOTS, ItemStack.EMPTY);
        for (int slot = from; slot <= to; slot++) {
            simulated.set(slot, inventory.get(slot).copy());
        }

        for (int i = 0; i < templates.size(); i++) {
            var stack = templates.get(i).create();

            if (i < extraChances.size() && extraChances.get(i) > 0) {
                stack.grow(1);
            }

            if (!insertShapelessly(simulated, stack, from, to, maxStackSize)) {
                return false;
            }
        }

        return true;
    }

    public static boolean insertShapelessly(NonNullList<ItemStack> inventory, ItemStack stack, int from, int to, int maxStackSize) {
        if (stack.isEmpty()) {
            return true;
        }

        for (int slot = from; slot <= to; slot++) {
            var current = inventory.get(slot);
            if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current, stack)) {
                int maxCount = Math.min(stack.getMaxStackSize(), Math.min(maxStackSize, stack.getMaxStackSize()));
                int available = maxCount - current.getCount();
                if (available > 0) {
                    int toAdd = Math.min(available, stack.getCount());
                    current.grow(toAdd);
                    stack.shrink(toAdd);
                    if (stack.isEmpty()) {
                        return true;
                    }
                }
            }
        }

        for (int slot = from; slot <= to; slot++) {
            var current = inventory.get(slot);
            if (current.isEmpty()) {
                int maxCount = Math.min(stack.getMaxStackSize(), Math.min(maxStackSize, stack.getMaxStackSize()));
                int toAdd = Math.min(maxCount, stack.getCount());
                inventory.set(slot, stack.copyWithCount(toAdd));
                stack.shrink(toAdd);
                if (stack.isEmpty()) {
                    return true;
                }
            }
        }

        return stack.isEmpty();
    }
}
