package io.github.reoseah.hayo.feature.cable;

import io.github.reoseah.hayo.feature.energy.EnergyTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class CableItem extends BlockItem {
    public final int transferLimit;

    public CableItem(CableBlock block, Properties properties) {
        super(block, properties);
        this.transferLimit = block.transferLimit;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        tooltipAdder.accept(EnergyTexts.maxAmountPerTick(this.transferLimit).withStyle(ChatFormatting.GRAY));
    }
}
