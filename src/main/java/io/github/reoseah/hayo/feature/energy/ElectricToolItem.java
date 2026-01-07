package io.github.reoseah.hayo.feature.energy;

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

public class ElectricToolItem extends Item implements ElectricItem {
    public static final DataComponentType<Integer> ENERGY = SimpleBatteryItem.ENERGY;

    public final int energyCost;
    public final int energyCapacity;
    public final int energyTransferLimit;

    public ElectricToolItem(Properties properties, int energyCost, int energyCapacity, int energyTransferLimit) {
        super(properties);
        this.energyCost = energyCost;
        this.energyCapacity = energyCapacity;
        this.energyTransferLimit = energyTransferLimit;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        return ElectricItems.tryUseEnergy(this.energyCost, stack, s -> {
        });
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (this.getEnergy(stack) >= this.energyCost && this.isCorrectToolForDrops(stack, state)) {
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
        return this.energyCapacity;
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
}
