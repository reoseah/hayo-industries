package hayo.common.item;

import hayo.energy.EnergyTexts;
import hayo.energy.item.EnergyComponents;
import net.minecraft.ChatFormatting;
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

public class SimpleElectricItem extends Item {
    public SimpleElectricItem(Properties properties) {
        super(properties);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(
            ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(EnergyTexts.amountAndCapacity(EnergyComponents.getEnergy(stack), EnergyComponents.getCapacity(stack)).withStyle(ChatFormatting.GRAY));

        var energyTool = stack.get(EnergyComponents.ENERGY_TOOL);
        if (energyTool != null) {
            builder.accept(EnergyTexts.amountPerUse(energyTool.destroyEnergy()).withStyle(ChatFormatting.GRAY));
        }
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

    // TODO: move this to mixins, so the components can be applied to any item and work correctly
    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        boolean correct = super.isCorrectToolForDrops(stack, state);
        var energyTool = stack.get(EnergyComponents.ENERGY_TOOL);
        if (energyTool != null) {
            return correct && EnergyComponents.getEnergy(stack) > energyTool.destroyEnergy();
        }
        return correct;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        var energyTool = stack.get(EnergyComponents.ENERGY_TOOL);
        if (energyTool != null) {
            return EnergyComponents.tryRemoveEnergy(energyTool.destroyEnergy(), stack);
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

            if (energy >= energyTool.destroyEnergy() && this.isCorrectToolForDrops(stack, state)) {
                return energyTool.chargedDestroySpeed();
            }
        }
        return super.getDestroySpeed(stack, state);
    }
}
