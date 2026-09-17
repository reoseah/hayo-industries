package hayo.solid_fluid_reactor;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.function.Predicate;

public record FluidIngredientAmount(HolderSet<Fluid> values, int amount) implements Predicate<FluidStack> {
    public static final FluidIngredientAmount EMPTY = new FluidIngredientAmount(HolderSet.empty(), 0);

    public static final MapCodec<FluidIngredientAmount> NON_EMPTY_CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    ExtraCodecs.nonEmptyHolderSet(
                                    HolderSetCodec.create(
                                            Registries.FLUID,
                                            BuiltInRegistries.FLUID.holderByNameCodec().validate(holder -> {
                                                if (holder.value() == Fluids.EMPTY) {
                                                    return DataResult.error(() -> "Fluid must not be minecraft:empty");
                                                }
                                                return DataResult.success(holder);
                                            }),
                                            false
                                    )
                            )
                            .fieldOf("id")
                            .forGetter(FluidIngredientAmount::values),
                    ExtraCodecs.POSITIVE_INT.fieldOf("amount").orElse(0).forGetter(FluidIngredientAmount::amount)
            )
            .apply(instance, FluidIngredientAmount::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidIngredientAmount> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderSet(Registries.FLUID),
            FluidIngredientAmount::values,
            ByteBufCodecs.VAR_INT,
            FluidIngredientAmount::amount,
            FluidIngredientAmount::new
    );

    @Override
    public boolean test(FluidStack stack) {
        return this.values.contains(stack.holder()) && stack.amount() >= this.amount;
    }
}
