package hayo.energy.client;

import com.mojang.serialization.MapCodec;
import hayo.energy.item.EnergyComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/// Energy in an item stack as a ranged property for item definitions.
///
/// E.g.:
/// ```json
/// {
///   "model": {
///     "type": "minecraft:range_dispatch",
///     "property": "hayo:energy",
///     "scale": 1,
///     "fallback": {
///       "type": "minecraft:model",
///       "model": "modid:item/example_battery"
///     },
///     "entries": [
///       {
///         "threshold": 1000,
///         "model": {
///           "type": "minecraft:model",
///           "model": "modid:item/example_battery_charge1"
///         }
///       },
///       // repeat for each texture+threshold you have
///     ]
///   }
/// }
/// ```
@Environment(EnvType.CLIENT)
public record EnergyModelProperty() implements RangeSelectItemModelProperty {
    public static final MapCodec<EnergyModelProperty> MAP_CODEC = MapCodec.unit(new EnergyModelProperty());

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        return ((float) EnergyComponents.getEnergy(stack));
    }

    @Override
    public MapCodec<? extends RangeSelectItemModelProperty> type() {
        return MAP_CODEC;
    }
}
