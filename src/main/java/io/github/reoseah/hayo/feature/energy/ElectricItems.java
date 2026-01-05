package io.github.reoseah.hayo.feature.energy;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/// Utilities for working with {@link ElectricItem}.
public class ElectricItems {
    public static boolean isElectric(ItemStack stack) {
        return stack.getItem() instanceof ElectricItem;
    }

    public static boolean isChargeable(ItemStack stack) {
        return stack.getItem() instanceof ElectricItem electricItem
                && electricItem.canCharge(stack);
    }

    public static boolean isDischargeable(ItemStack stack) {
        return stack.getItem() instanceof ElectricItem electricItem
                && electricItem.canDischarge(stack);
    }

    public static int tryGetEnergy(ItemStack stack) {
        if (stack.getItem() instanceof ElectricItem electricItem) {
            return electricItem.getEnergy(stack);
        }
        return 0;
    }

    public static int tryGetCapacity(ItemStack stack) {
        if (stack.getItem() instanceof ElectricItem electricItem) {
            return electricItem.getEnergyCapacity(stack);
        }
        return 0;
    }

    /// Charge the item or do nothing if the item is not electric.
    ///
    /// @return energy that was added to the item, you probably want to remove it from your energy source
    public static int tryCharge(int energy, ItemStack stack, Consumer<ItemStack> setItem) {
        if (stack.getItem() instanceof ElectricItem electricItem) {
            return electricItem.charge(stack, energy, setItem);
        }
        return 0;
    }

    /// Try to charge an item in a container slot using [Container#setItem(int, ItemStack)].
    ///
    /// @return energy that was added to the item, you probably want to remove it from your energy source
    public static int tryCharge(int max, Container container, int slot) {
        return tryCharge(max, container.getItem(slot), stack -> container.setItem(slot, stack));
    }

    /// Discharge the item or do nothing if the item is not electric.
    ///
    /// @return energy that was removed from the item, you probably want to add it to your energy storage
    public static int tryDischarge(int max, ItemStack stack, Consumer<ItemStack> setItem) {
        if (stack.getItem() instanceof ElectricItem electricItem) {
            return electricItem.discharge(stack, max, setItem);
        }
        return 0;
    }

    /// Try to charge an item in a container slot using [Container#setItem(int, ItemStack)].
    ///
    /// @return energy that was removed from the item, you probably want to add it to your energy storage
    public static int tryDischarge(int max, Container container, int slot) {
        return tryDischarge(max, container.getItem(slot), stack -> container.setItem(slot, stack));
    }
}
