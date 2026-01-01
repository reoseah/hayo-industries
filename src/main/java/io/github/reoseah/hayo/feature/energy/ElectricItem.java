package io.github.reoseah.hayo.feature.energy;

import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/// Interface for items that can be charged and discharged.
public interface ElectricItem {
    int getEnergy(ItemStack stack);

    int getEnergyCapacity(ItemStack stack);

    /// Returns a stack with the given energy. Can mutate the argument
    /// and return it or can return a different item stack.
    ItemStack setEnergy(ItemStack stack, int amount);

    /// Returns limit of how much energy can be inserted or extracted per
    /// "charge" or "discharge" call. (It is assumed that
    /// "charge" or "discharge" are only called once per tick.)
    int getEnergyTransferLimit(ItemStack stack);

    default boolean canCharge(ItemStack stack) {
        return this.getEnergy(stack) < this.getEnergyCapacity(stack);
    }

    default boolean canDischarge(ItemStack stack) {
        return this.getEnergy(stack) > 0;
    }

    /// @param setItem callback for updated stack, e.g. `(stack) -> { this.stacks.set(i, stack); this.setChanged(); }`
    /// @return energy that was added to the item, you want to remove it from your energy source
    default int charge(ItemStack stack, int amount, Consumer<ItemStack> setItem) {
        if (!this.canCharge(stack) || stack.getCount() > 1) {
            return 0;
        }
        int change = Math.min(Math.min(this.getEnergyCapacity(stack) - this.getEnergy(stack), amount), this.getEnergyTransferLimit(stack));
        setItem.accept(this.setEnergy(stack, this.getEnergy(stack) + change));
        return change;
    }

    /// @param setItem callback for updated stack, e.g. `(stack) -> { this.stacks.set(i, stack); this.setChanged(); }`
    /// @return energy that was removed from the item, you want to add it to your energy source
    default int discharge(ItemStack stack, int max, Consumer<ItemStack> setItem) {
        if (!this.canDischarge(stack) || stack.getCount() > 1) {
            return 0;
        }
        int change = Math.min(Math.min(this.getEnergy(stack), max), this.getEnergyTransferLimit(stack));
        setItem.accept(this.setEnergy(stack, this.getEnergy(stack) - change));
        return change;
    }
}


