package io.github.reoseah.hayoind.item;

import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/**
 * Interface for items that can be charged and discharged.
 * Also contains static methods for common operations.
 * <p>
 * Calls to "charge" and "discharge" pass
 * modified stack to a callback, while returning
 * the change in item energy.
 */
public interface ElectricItem {
    int getEnergy(ItemStack stack);

    int getEnergyCapacity(ItemStack stack);

    /**
     * Returns a stack with the given energy. Can mutate stack
     * and return it or return a completely different one!
     */
    ItemStack setEnergy(ItemStack stack, int amount);

    /**
     * Limits how much energy can be inserted or extracted per
     * "charge" or "discharge" call. It is _assumed_ that
     * "charge" or "discharge" will only be called once per tick,
     * but not guaranteed.
     */
    int getEnergyTransferLimit(ItemStack stack);

    default boolean canCharge(ItemStack stack) {
        return this.getEnergy(stack) < this.getEnergyCapacity(stack);
    }

    default boolean canDischarge(ItemStack stack) {
        return this.getEnergy(stack) > 0;
    }

    default int charge(ItemStack stack, int amount, Consumer<ItemStack> stackSaver) {
        if (!this.canCharge(stack) || stack.getCount() > 1) {
            return 0;
        }
        int change = Math.min(Math.min(this.getEnergyCapacity(stack) - this.getEnergy(stack), amount), this.getEnergyTransferLimit(stack));
        stackSaver.accept(this.setEnergy(stack, this.getEnergy(stack) + change));
        return change;
    }

    default int discharge(ItemStack stack, int max, Consumer<ItemStack> stackSaver) {
        if (!this.canDischarge(stack) || stack.getCount() > 1) {
            return 0;
        }
        int change = Math.min(Math.min(this.getEnergy(stack), max), this.getEnergyTransferLimit(stack));
        stackSaver.accept(this.setEnergy(stack, this.getEnergy(stack) - change));
        return change;
    }

    static boolean isElectricItem(ItemStack stack) {
        return stack.getItem() instanceof ElectricItem;
    }

    static boolean isChargeable(ItemStack stack) {
        return stack.getItem() instanceof ElectricItem electricItem
                && electricItem.canCharge(stack);
    }

    static boolean isDischargeable(ItemStack stack) {
        return stack.getItem() instanceof ElectricItem electricItem
                && electricItem.canDischarge(stack);
    }

    static int tryGetEnergy(ItemStack stack) {
        if (stack.getItem() instanceof ElectricItem electricItem) {
            return electricItem.getEnergy(stack);
        }
        return 0;
    }

    static int tryGetEnergyCapacity(ItemStack stack) {
        if (stack.getItem() instanceof ElectricItem electricItem) {
            return electricItem.getEnergyCapacity(stack);
        }
        return 0;
    }

    /**
     * Utility method to charge an item or do nothing if the item is not electric.
     *
     * @return energy that was added to the item, you probably want to remove it from your energy source
     */
    static int tryCharge(int energy, ItemStack stack, Consumer<ItemStack> stackSaver) {
        if (stack.getItem() instanceof ElectricItem electricItem) {
            return electricItem.charge(stack, energy, stackSaver);
        }
        return 0;
    }

    /**
     * Utility method to discharge an item or do nothing if the item is not electric.
     *
     * @return energy that was removed from the item, you probably want to add it to your energy source
     */
    static int tryDischarge(int max, ItemStack stack, Consumer<ItemStack> stackSaver) {
        if (stack.getItem() instanceof ElectricItem electricItem) {
            return electricItem.discharge(stack, max, stackSaver);
        }
        return 0;
    }
}


