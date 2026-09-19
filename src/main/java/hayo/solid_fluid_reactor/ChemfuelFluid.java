package hayo.solid_fluid_reactor;

import hayo.Hayo;
import hayo.common.fluid.HayoFluid;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public abstract class ChemfuelFluid extends HayoFluid {
    @Override
    public Item getBucket() {
        return Hayo.Items.CHEMFUEL_BUCKET;
    }

    @Override
    public Fluid getSource() {
        return Hayo.Fluids.CHEMFUEL;
    }

    @Override
    public Fluid getFlowing() {
        return Hayo.Fluids.FLOWING_CHEMFUEL;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState fluidState) {
        return Hayo.Blocks.CHEMFUEL.defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(fluidState));
    }

    @Override
    public boolean isSame(Fluid other) {
        return other == Hayo.Fluids.CHEMFUEL || other == Hayo.Fluids.FLOWING_CHEMFUEL;
    }

    public static class Source extends ChemfuelFluid {
        @Override
        public boolean isSource(FluidState fluidState) {
            return true;
        }
    }

    public static class Flowing extends ChemfuelFluid {
        @Override
        public boolean isSource(FluidState fluidState) {
            return false;
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }
    }
}
