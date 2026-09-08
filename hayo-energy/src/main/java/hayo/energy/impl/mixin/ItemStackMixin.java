package hayo.energy.impl.mixin;

import hayo.energy.item.EnergyComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements DataComponentHolder {
    @Inject(method = "addAttributeTooltips", at = @At("RETURN"))
    private void addAttributeTooltips(Consumer<Component> consumer, TooltipDisplay display, @Nullable Player player, CallbackInfo ci) {
        if (display.shows(DataComponents.ATTRIBUTE_MODIFIERS)) {
            var energyArmor = this.get(EnergyComponents.ENERGY_ARMOR);
            if (energyArmor != null && energyArmor.energyPerDamage() != 0) {
                consumer.accept(Component.translatable("hayo.energy_per_damage", energyArmor.energyPerDamage()).withStyle(ChatFormatting.DARK_AQUA));
            }
        }
    }
}
