package hayo.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import hayo.Hayo;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @ModifyReturnValue(method = "getDamageAfterArmorAbsorb", at = @At("RETURN"))
    public float onApplyArmorToDamage(float original) {
        int totalDamageReduction = 0;
        for (var slot : EquipmentSlotGroup.ARMOR) {
            var item = ((LivingEntity) (Object) this).getItemBySlot(slot);
            totalDamageReduction += item.getOrDefault(Hayo.Components.DAMAGE_REDUCTION, 0);
        }

        return original * (100 - totalDamageReduction) / 100;
    }
}
