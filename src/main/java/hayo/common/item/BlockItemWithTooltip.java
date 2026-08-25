package hayo.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.function.Consumer;

public class BlockItemWithTooltip extends BlockItem {
    protected final List<Component> tooltip;

    public BlockItemWithTooltip(Block block, Properties properties, Component tooltip) {
        this(block, properties, List.of(tooltip));
    }

    public BlockItemWithTooltip(Block block, Properties properties, Component... tooltip) {
        this(block, properties, List.of(tooltip));
    }

    public BlockItemWithTooltip(Block block, Properties properties, List<Component> tooltip) {
        super(block, properties);
        this.tooltip = tooltip;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, builder, tooltipFlag);

        for (var component : this.tooltip) {
            builder.accept(component);
        }
    }
}
