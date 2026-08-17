package hayo.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import hayo.Hayo;
import hayo.energy.item.EnergyComponents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @ModifyReturnValue(method = "getDamageAfterArmorAbsorb", at = @At("RETURN"))
    public float onApplyArmorToDamage(float original) {
        int quantumPieces = 0;
        for (var slot : EquipmentSlotGroup.ARMOR) {
            var item = ((LivingEntity) (Object) this).getItemBySlot(slot);
            if (item.has(Hayo.Components.QUANTUM_ARMOR)) {
                quantumPieces++;
            }
        }

        return original * (1 - 0.25F * quantumPieces);
    }

    @Inject(at = @At("RETURN"), method = "doHurtEquipment")
    public void doHurtEquipment(DamageSource damageSource, float damage, EquipmentSlot[] slots, CallbackInfo ci) {
        if (damage > 0) {
            int durabilityDamage = (int) Math.max(1.0F, damage / 4.0F);
            for (var slot : slots) {
                var stack = ((LivingEntity) (Object) this).getItemBySlot(slot);
                var energyArmor = stack.get(EnergyComponents.ENERGY_ARMOR);
                if (energyArmor != null) {
                    EnergyComponents.setEnergy(stack, Math.max(0, EnergyComponents.getEnergy(stack) - energyArmor.energyPerDamage() * durabilityDamage));
                }
            }
        }
    }
}
