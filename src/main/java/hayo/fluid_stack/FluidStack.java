package hayo.fluid_stack;

import com.mojang.serialization.Codec;
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
    public static final Codec<FluidStack> CODEC = MAP_CODEC.codec();
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.FLUID),
            FluidStack::holder,
            ByteBufCodecs.VAR_INT,
            FluidStack::amount,
            FluidStack::new
    );

    public FluidStack {
        if (amount == 0) {
            holder = Fluids.EMPTY.builtInRegistryHolder();
        }
    }

    public FluidStack(Fluid fluid, int amount) {
        this(fluid.builtInRegistryHolder(), amount);
    }

    public Fluid fluid() {
        return this.holder.value();
    }

    public boolean isEmpty() {
        return this.holder == Fluids.EMPTY.builtInRegistryHolder() || this.amount == 0;
    }
}
