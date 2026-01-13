package io.github.reoseah.hayo.feature.batpack;

import io.github.reoseah.hayo.feature.energy.item.ElectricItem;
import io.github.reoseah.hayo.feature.energy.item.EnergyComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BatpackItem extends ElectricItem {
    public BatpackItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (level.isClientSide()) {
            return;
        }
        if (slot == EquipmentSlot.CHEST && entity instanceof LivingEntity livingEntity) {
            var mainhandStack = livingEntity.getItemBySlot(EquipmentSlot.MAINHAND);
            if (mainhandStack.has(EnergyComponents.ENERGY_STORAGE)) {
                EnergyComponents.moveEnergy(stack, mainhandStack);
            }
        }
    }
}
