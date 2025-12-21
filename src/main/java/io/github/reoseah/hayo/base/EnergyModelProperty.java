package io.github.reoseah.hayo.base;

import com.mojang.serialization.MapCodec;
import io.github.reoseah.hayo.api.energy.ElectricItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public record EnergyModelProperty() implements RangeSelectItemModelProperty {
    public static final MapCodec<EnergyModelProperty> MAP_CODEC = MapCodec.unit(new EnergyModelProperty());

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        return ((float) ElectricItems.tryGetEnergy(stack));
    }

    @Override
    public MapCodec<? extends RangeSelectItemModelProperty> type() {
        return MAP_CODEC;
    }
}
