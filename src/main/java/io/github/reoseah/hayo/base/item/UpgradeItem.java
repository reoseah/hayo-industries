package io.github.reoseah.hayo.base.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class UpgradeItem extends Item {
    public final @Nullable Component targetMachine;
    public final List<MutableComponent> effects;

    public UpgradeItem(Properties properties, List<Component> effects) {
        this(properties, null, effects);
    }

    public UpgradeItem(Properties properties, @Nullable Component targetMachine, List<Component> effects) {
        super(properties);
        this.targetMachine = targetMachine;
        this.effects = effects.stream().map(component -> {
            if (component instanceof MutableComponent mutable) {
                return mutable;
            }
            return MutableComponent.create(component.getContents());
        }).toList();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);

        builder.accept(Component.empty());
        if (this.targetMachine != null) {
            builder.accept(Component.translatable("hayo.upgrades.when_in_machine", this.targetMachine).withStyle(ChatFormatting.GRAY));
        } else {
            builder.accept(Component.translatable("hayo.upgrades.when_in_a_valid_machine").withStyle(ChatFormatting.GRAY));
        }

        for (var effect : this.effects) {
            builder.accept(effect.withStyle(ChatFormatting.DARK_AQUA));
        }
    }
}
