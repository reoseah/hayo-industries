package io.github.reoseah.hayo.feature.energy;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class SimpleBatteryItem extends Item implements ElectricItem {
    public static final DataComponentType<Integer> ENERGY = DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build();

    public final int energyCapacity;
    public final int energyTransferLimit;

    public SimpleBatteryItem(Properties properties, int energyCapacity, int energyTransferLimit) {
        super(properties.stacksTo(1));
        this.energyCapacity = energyCapacity;
        this.energyTransferLimit = energyTransferLimit;
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
        return ElectricItems.defaultIsBarVisible(this, stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return ElectricItems.defaultBarWidth(this, stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ElectricItems.defaultBarColor(this, stack);
    }
}
