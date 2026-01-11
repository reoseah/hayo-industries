package io.github.reoseah.hayo.mixin;

import io.github.reoseah.hayo.feature.energy.items.SimpleElectricArmorItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(at = @At("RETURN"), method = "doHurtEquipment")
    public void doHurtEquipment(DamageSource damageSource, float damage, EquipmentSlot[] slots, CallbackInfo ci) {
        if (damage > 0) {
            int durabilityDamage = (int) Math.max(1.0F, damage / 4.0F);
            for (var slot : slots) {
                var stack = ((LivingEntity) (Object) this).getItemBySlot(slot);
                if (stack.getItem() instanceof SimpleElectricArmorItem armor) {
                    armor.setEnergy(stack, Math.max(0, armor.getEnergy(stack) - armor.energyCost * durabilityDamage));
                }
            }
        }
    }
}
