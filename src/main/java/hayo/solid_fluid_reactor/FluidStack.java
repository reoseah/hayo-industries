package hayo.solid_fluid_reactor;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public record FluidStack(Holder<Fluid> holder, int amount) {
    public static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY.builtInRegistryHolder(), 0);

    public static final MapCodec<FluidStack> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    BuiltInRegistries.FLUID.holderByNameCodec().fieldOf("id").forGetter(FluidStack::holder),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("amount").forGetter(FluidStack::amount)
            )
            .apply(instance, FluidStack::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.FLUID),
            FluidStack::holder,
            ByteBufCodecs.VAR_INT,
            FluidStack::amount,
            FluidStack::new
    );

    public Fluid fluid() {
        return this.holder.value();
    }

    public boolean isEmpty() {
        return this.amount == 0;
    }
}
