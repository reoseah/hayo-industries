package io.github.reoseah.hayo.feature.energy;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/// Utilities for working with {@link ElectricItem}.
public class ElectricItems {
    public static boolean isElectric(ItemStack stack) {
        return stack.getItem() instanceof ElectricItem;
    }

    public static boolean canCharge(ItemStack stack) {
        return stack.getItem() instanceof ElectricItem electricItem
                && electricItem.canCharge(stack);
    }

    public static boolean canDischarge(ItemStack stack) {
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

    public static boolean tryUseEnergy(int amount, ItemStack stack, Consumer<ItemStack> setItem) {
        if (stack.getItem() instanceof ElectricItem electricItem) {
            if (stack.getCount() > 1) {
                return false;
            }
            int storedEnergy = electricItem.getEnergy(stack);
            if (storedEnergy < amount) {
                return false;
            }
            setItem.accept(electricItem.setEnergy(stack, storedEnergy - amount));
            return true;
        }
        return false;
    }

    public static boolean defaultIsBarVisible(ElectricItem item, ItemStack stack) {
        if (stack.getCount() != 1) {
            return false;
        }

        int energy = item.getEnergy(stack);
        int capacity = item.getEnergyCapacity(stack);
        return energy != 0 && energy < capacity;
    }

    public static int defaultBarWidth(ElectricItem item, ItemStack stack) {
        return Math.round(13F * ((float) item.getEnergy(stack)) / item.getEnergyCapacity(stack));
    }

    public static int defaultBarColor(ElectricItem item, ItemStack stack) {
        float ratio = 1F - ((float) item.getEnergy(stack)) / (float) item.getEnergyCapacity(stack);

        // from blue to red
        float hue = Mth.lerp(ratio, 240F, 360F) / 360F;
        // from 50% to 100% saturation, otherwise pure blue is too dark
        float saturation = Mth.lerp(ratio, 0.5F, 1F);
        return Mth.hsvToRgb(hue, saturation, 1.0F);
    }

    public static void defaultTooltip(ElectricItem item, ItemStack stack, Consumer<Component> tooltipAdder) {
        tooltipAdder.accept(EnergyTexts.amountAndCapacity(item.getEnergy(stack), item.getEnergyCapacity(stack)).withStyle(ChatFormatting.GRAY));
    }

    public static ItemStack withCharge(Item item, int charge) {
        if (item instanceof ElectricItem electricItem) {
            var stack = new ItemStack(item);
            electricItem.setEnergy(stack, charge);
            return stack;
        }
        throw new UnsupportedOperationException();
    }

    public static ItemStack withFullCharge(Item item) {
        if (item instanceof ElectricItem electricItem) {
            var stack = new ItemStack(item);
            electricItem.setEnergy(stack, electricItem.getEnergyCapacity(stack));
            return stack;
        }
        throw new UnsupportedOperationException();
    }
}