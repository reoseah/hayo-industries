package io.github.reoseah.hayo.feature.electric_tools;

import io.github.reoseah.hayo.feature.energy.ElectricItem;
import io.github.reoseah.hayo.feature.energy.ElectricItems;
import io.github.reoseah.hayo.feature.energy.SimpleBatteryItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public class DrillItem extends Item implements ElectricItem {
    public static final DataComponentType<Integer> ENERGY = SimpleBatteryItem.ENERGY;

    public static final int ENERGY_COST = 400;
    public static final int ENERGY_CAPACITY = 10000;
    public static final int ENERGY_TRANSFER_LIMIT = 32;

    public DrillItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        return ElectricItems.tryUseEnergy(ENERGY_COST, stack, s -> {
        });
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (this.getEnergy(stack) >= ENERGY_COST && this.isCorrectToolForDrops(stack, state)) {
            return super.getDestroySpeed(stack, state);
        }
        return 0.5F;
    }

    @Override
    public int getEnergy(ItemStack stack) {
        return stack.getOrDefault(ENERGY, 0);
    }

    @Override
    public int getEnergyCapacity(ItemStack stack) {
        return ENERGY_CAPACITY;
    }

    @Override
    public boolean canDischarge(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack setEnergy(ItemStack stack, int amount) {
        stack.set(ENERGY, amount);
        return stack;
    }

    @Override
    public int getEnergyTransferLimit(ItemStack stack) {
        return ENERGY_TRANSFER_LIMIT;
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
