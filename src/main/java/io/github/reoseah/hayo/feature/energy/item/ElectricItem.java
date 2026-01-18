package io.github.reoseah.hayo.feature.energy.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public class ElectricItem extends Item {
    public ElectricItem(Properties properties) {
        super(properties);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        EnergyComponents.defaultTooltip(stack, tooltipAdder);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return EnergyComponents.defaultIsBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return EnergyComponents.defaultBarWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return EnergyComponents.defaultBarColor(stack);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        boolean correct = super.isCorrectToolForDrops(stack, state);
        var energyTool = stack.get(EnergyComponents.ENERGY_TOOL);
        if (energyTool != null) {
            return correct && EnergyComponents.getEnergy(stack) > energyTool.miningEnergy();
        }
        return correct;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        var energyTool = stack.get(EnergyComponents.ENERGY_TOOL);
        if (energyTool != null) {
            return EnergyComponents.tryRemoveEnergy(energyTool.miningEnergy(), stack);
        }
        return super.mineBlock(stack, level, state, pos, entity);
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity mob, LivingEntity attacker) {
        var energyTool = stack.get(EnergyComponents.ENERGY_TOOL);
        if (energyTool != null) {
            EnergyComponents.tryRemoveEnergy(energyTool.attackEnergy(), stack);
        }
        super.postHurtEnemy(stack, mob, attacker);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        var energyTool = stack.get(EnergyComponents.ENERGY_TOOL);
        if (energyTool != null) {
            var energy = stack.getOrDefault(EnergyComponents.ENERGY, 0);

            if (energy >= energyTool.miningEnergy() && this.isCorrectToolForDrops(stack, state)) {
                return energyTool.chargedMiningSpeed();
            }
        }
        return super.getDestroySpeed(stack, state);
    }
}
