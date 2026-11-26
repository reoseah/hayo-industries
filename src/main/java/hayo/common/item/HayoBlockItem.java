package hayo.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class HayoBlockItem extends BlockItem {
    public HayoBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    public interface TooltipProvider {
        void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, builder, tooltipFlag);

        if (this.getBlock() instanceof TooltipProvider tooltipProvider) {
            tooltipProvider.appendHoverText(stack, context, display, builder, tooltipFlag);
        }
    }
}
