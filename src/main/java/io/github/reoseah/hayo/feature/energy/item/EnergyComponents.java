package io.github.reoseah.hayo.feature.energy.item;

import com.mojang.serialization.Codec;
import io.github.reoseah.hayo.feature.energy.EnergyTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class EnergyComponents {
    /// Provides energy capacity and transfer rate that an item should have.
    ///
    /// Note: for "energy storages" in more narrow sense (batteries, energy crystals)
    /// you'd also want to add [#BATTERY] to your item.
    public static final DataComponentType<EnergyStorage> ENERGY_STORAGE = DataComponentType.<EnergyStorage>builder() //
            .persistent(EnergyStorage.CODEC) //
            .networkSynchronized(EnergyStorage.STREAM_CODEC) //
            .build();

    /// Stores amount of energy in an item.
    ///
    /// Note: do not set values of this component directly, use [#setEnergy] or other
    /// methods in this class, so that [#CHARGED_ATTRIBUTES] can be applied or removed correctly.
    public static final DataComponentType<Integer> ENERGY = DataComponentType.<Integer>builder() //
            .persistent(Codec.INT) //
            .networkSynchronized(ByteBufCodecs.VAR_INT) //
            .ignoreSwapAnimation() //
            .build();

    /// Marks an item as capable to charge machines, energy storages, etc.
    public static final DataComponentType<Unit> BATTERY = DataComponentType.<Unit>builder() //
            .persistent(Unit.CODEC) //
            .networkSynchronized(Unit.STREAM_CODEC) //
            .build();

    /// Provides an amount of energy and item attributes that a stack should have when
    /// above the said amount of energy. Use this for electric tools, weapons or armor.
    public static final DataComponentType<ChargedAttributes> CHARGED_ATTRIBUTES = DataComponentType.<ChargedAttributes>builder() //
            .persistent(ChargedAttributes.CODEC) //
            .networkSynchronized(ChargedAttributes.STREAM_CODEC) //
            .build();

    /// Supplements default tool attribute with energy cost and charged mining speed.
    public static final DataComponentType<EnergyTool> ENERGY_TOOL = DataComponentType.<EnergyTool>builder() //
            .persistent(EnergyTool.CODEC) //
            .networkSynchronized(EnergyTool.STREAM_CODEC) //
            .build();

    /// Supplements default armor attributes with energy cost.
    public static final DataComponentType<EnergyArmor> ENERGY_ARMOR = DataComponentType.<EnergyArmor>builder() //
            .persistent(EnergyArmor.CODEC) //
            .networkSynchronized(EnergyArmor.STREAM_CODEC) //
            .build();

    public static boolean isStorage(ItemStack stack) {
        return stack.has(ENERGY_STORAGE);
    }

    public static boolean canChargeInMachine(ItemStack stack) {
        return stack.has(ENERGY_STORAGE);
    }

    public static boolean canDischargeInMachine(ItemStack stack) {
        return stack.has(ENERGY_STORAGE) && stack.has(BATTERY);
    }

    public static int getEnergy(ItemStack stack) {
        return stack.getOrDefault(ENERGY, 0);
    }

    public static void setEnergy(ItemStack stack, int energy) {
        stack.set(ENERGY, energy);
        updateEnergyComponents(stack, energy);
    }

    public static void updateEnergyComponents(ItemStack stack, int energy) {
        var chargedAttributes = stack.get(CHARGED_ATTRIBUTES);
        if (chargedAttributes != null) {
            boolean hasCharge = energy >= chargedAttributes.requiredEnergy();

            var attributes = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
            boolean hasChargedAttributes = attributes == chargedAttributes.attributes();

            if (hasCharge && !hasChargedAttributes) {
                stack.set(DataComponents.ATTRIBUTE_MODIFIERS, chargedAttributes.attributes());
            } else if (!hasCharge && hasChargedAttributes) {
                stack.remove(DataComponents.ATTRIBUTE_MODIFIERS);
            }
        }
    }

    public static int getCapacity(ItemStack stack) {
        var storage = stack.get(ENERGY_STORAGE);
        return storage != null ? storage.capacity() : 0;
    }

    /// Charge the item or do nothing if the item is not electric.
    ///
    /// @return energy that was added to the item, you probably want to remove it from your energy source
    public static int charge(int energy, ItemStack stack) {
        var storage = stack.get(ENERGY_STORAGE);
        if (storage == null) {
            return 0;
        }
        int stackEnergy = stack.getOrDefault(ENERGY, 0);
        int stackCapacity = storage.capacity();
        if (stackEnergy >= stackCapacity || stack.getCount() > 1) {
            return 0;
        }
        int change = Math.min(Math.min(stackCapacity - stackEnergy, energy), storage.transferLimit());

        setEnergy(stack, stackEnergy + change);
        return change;
    }

    /// Discharge the item or do nothing if the item is not electric.
    ///
    /// @return energy that was removed from the item, you probably want to add it to your energy storage
    public static int discharge(int amount, ItemStack stack) {
        if (!stack.has(BATTERY)) {
            return 0;
        }
        var storage = stack.get(ENERGY_STORAGE);
        if (storage == null) {
            return 0;
        }
        int stackEnergy = stack.getOrDefault(ENERGY, 0);
        if (stackEnergy <= 0 || stack.getCount() > 1) {
            return 0;
        }
        int change = Math.min(Math.min(stackEnergy, amount), storage.transferLimit());
        setEnergy(stack, stackEnergy - change);
        return change;
    }

    /// Removes the specified amount of energy from item and returns true,
    /// otherwise returns false.
    public static boolean tryUseEnergy(int amount, ItemStack stack) {
        if (stack.getCount() > 1) {
            return false;
        }
        int storedEnergy = stack.getOrDefault(ENERGY, 0);
        if (storedEnergy < amount) {
            return false;
        }
        setEnergy(stack, storedEnergy - amount);
        return true;
    }

    public static ItemStack withFullCharge(Item item) {
        var stack = new ItemStack(item);
        setEnergy(stack, getCapacity(stack));
        return stack;
    }

    public static boolean defaultIsBarVisible(ItemStack stack) {
        if (stack.getCount() != 1) {
            return false;
        }
        int energy = stack.getOrDefault(ENERGY, 0);
        int capacity = getCapacity(stack);
        if (energy >= capacity) {
            return false;
        }
        var tool = stack.get(ENERGY_TOOL);
        if (tool != null) {
            return energy >= Math.min(tool.miningEnergy(), tool.attackEnergy());
        }
        return energy > 0;
    }

    public static int defaultBarWidth(ItemStack stack) {
        return Math.round(13F * ((float) (int) stack.getOrDefault(ENERGY, 0)) / getCapacity(stack));
    }

    public static int defaultBarColor(ItemStack stack) {
        float ratio = 1F - ((float) (int) stack.getOrDefault(ENERGY, 0)) / (float) getCapacity(stack);

        // from blue to red
        float hue = Mth.lerp(ratio, 240F, 360F) / 360F;
        // from 50% to 100% saturation, otherwise pure blue appears too dark
        float saturation = Mth.lerp(ratio, 0.5F, 1F);
        return Mth.hsvToRgb(hue, saturation, 1.0F);
    }

    public static void defaultTooltip(ItemStack stack, Consumer<Component> tooltipAdder) {
        tooltipAdder.accept(EnergyTexts.amountAndCapacity(stack.getOrDefault(ENERGY, 0), getCapacity(stack)).withStyle(ChatFormatting.GRAY));
    }

    public static int moveEnergy(ItemStack source, ItemStack target) {
        var sourceStorage = source.get(ENERGY_STORAGE);
        var targetStorage = target.get(ENERGY_STORAGE);

        if (sourceStorage == null || targetStorage == null) {
            return 0;
        }

        var transferRate = Math.min(sourceStorage.transferLimit(), targetStorage.transferLimit());
        var sourceEnergy = getEnergy(source);
        int targetEnergy = getEnergy(target);

        var transfer = Math.min(transferRate, Math.min(sourceEnergy, targetStorage.capacity() - targetEnergy));
        setEnergy(target, targetEnergy + transfer);
        setEnergy(source, sourceEnergy - transfer);

        return transfer;
    }
}
