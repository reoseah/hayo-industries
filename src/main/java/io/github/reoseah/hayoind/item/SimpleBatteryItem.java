package io.github.reoseah.hayoind.item;

import com.mojang.serialization.Codec;
import io.github.reoseah.hayoind.EnergyFormatting;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.Mth;
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
        super(properties);
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
        tooltipAdder.accept(EnergyFormatting.energyAndCapacity(this.getEnergy(stack), this.getEnergyCapacity(stack)).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        if (stack.getCount() != 1) {
            return false;
        }
        int energy = this.getEnergy(stack);
        return energy != 0 && energy < this.getEnergyCapacity(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13 * ((float) this.getEnergy(stack)) / this.getEnergyCapacity(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float ratio = ((float) this.getEnergy(stack)) / (float) this.getEnergyCapacity(stack);

        float hue = Mth.lerp(ratio, 240F, 360F) / 360F; // from blue to red
        float saturation = Mth.lerp(ratio, 50, 100) / 100F; // from 50% to 100% saturation, otherwise pure blue is too dark
        return Mth.hsvToRgb(hue / 360F, saturation / 100F, 1.0F);
    }
}
