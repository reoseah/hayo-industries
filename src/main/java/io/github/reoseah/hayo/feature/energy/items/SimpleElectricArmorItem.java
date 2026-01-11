package io.github.reoseah.hayo.feature.energy.items;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class SimpleElectricArmorItem extends Item implements ElectricItem {
    public static final DataComponentType<Integer> ENERGY = SimpleBatteryItem.ENERGY;

    public final ItemAttributeModifiers chargedAttributes;
    public final int energyCost;
    public final int energyCapacity;
    public final int energyTransferLimit;

    public SimpleElectricArmorItem(Properties properties, ItemAttributeModifiers chargedAttributes, int energyCost, int energyCapacity, int energyTransferLimit) {
        super(properties);
        this.chargedAttributes = chargedAttributes;
        this.energyCost = energyCost;
        this.energyCapacity = energyCapacity;
        this.energyTransferLimit = energyTransferLimit;
    }

    @Override
    public boolean canDischarge(ItemStack stack) {
        return false;
    }

    @Override
    public int getEnergy(ItemStack stack) {
        return stack.getOrDefault(ENERGY, 0);
    }

    @Override
    public int getEnergyCapacity(ItemStack stack) {
        return this.energyCapacity;
    }

    @Override
    public ItemStack setEnergy(ItemStack stack, int amount) {
        stack.set(ENERGY, amount);
        this.updateAttributeModifiers(stack, amount);

        return stack;
    }

    @Override
    public int getEnergyTransferLimit(ItemStack stack) {
        return this.energyTransferLimit;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        ElectricItems.defaultTooltip(this, stack, tooltipAdder);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return this.getEnergy(stack) < this.getEnergyCapacity(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return ElectricItems.defaultBarWidth(this, stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ElectricItems.defaultBarColor(this, stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        int energy = stack.getOrDefault(ENERGY, 0);

        this.updateAttributeModifiers(stack, energy);
    }

    public void updateAttributeModifiers(ItemStack stack, int energy) {
        var attributes = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (energy >= this.energyCost && attributes != this.chargedAttributes) {
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, this.chargedAttributes);
        } else if (stack.getOrDefault(ENERGY, 0) < this.energyCost && attributes == this.chargedAttributes) {
            stack.remove(DataComponents.ATTRIBUTE_MODIFIERS);
        }
    }
}
