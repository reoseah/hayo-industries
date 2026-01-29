package io.github.reoseah.hayo.base.item;

import io.github.reoseah.hayo.feature.cable.CableBlock;
import io.github.reoseah.hayo.feature.energy.EnergyTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class BlockItemWithTooltip extends BlockItem {
    protected final Component tooltip;

    public BlockItemWithTooltip(Block block, Component tooltip, Properties properties) {
        super(block, properties);
        this.tooltip = tooltip;
    }

    public BlockItemWithTooltip(CableBlock block, Properties properties) {
        this(block, EnergyTexts.maxAmountPerTick(block.transferLimit).withStyle(ChatFormatting.GRAY), properties);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        tooltipAdder.accept(this.tooltip);
    }
}
