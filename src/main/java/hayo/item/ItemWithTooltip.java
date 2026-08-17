package hayo.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ItemWithTooltip extends Item {
    public final List<Component> effects;

    public ItemWithTooltip(Properties properties, Component... effects) {
        super(properties);
        this.effects = Arrays.asList(effects);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);

        for (var effect : this.effects) {
            builder.accept(effect);
        }
    }
}
