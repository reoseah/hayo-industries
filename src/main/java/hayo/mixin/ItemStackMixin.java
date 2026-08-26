package hayo.mixin;

import hayo.Hayo;
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
            int damageReduction = this.getOrDefault(Hayo.Components.DAMAGE_REDUCTION, 0);
            int energy = this.getOrDefault(EnergyComponents.ENERGY, 0);
            var energyArmor = this.get(EnergyComponents.ENERGY_ARMOR);
            if (damageReduction > 0 && energyArmor != null && energy > energyArmor.energyPerDamage()) {
                consumer.accept(Component.translatable("hayo.damage_reduction", damageReduction).withStyle(ChatFormatting.DARK_AQUA));
            }
        }
    }
}
