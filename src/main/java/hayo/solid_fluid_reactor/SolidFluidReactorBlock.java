package hayo.solid_fluid_reactor;

import com.mojang.serialization.MapCodec;
import hayo.Hayo;
import hayo.common.block.HorizontalDirectionalElectricalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class SolidFluidReactorBlock extends HorizontalDirectionalElectricalBlock {
    public static final MapCodec<SolidFluidReactorBlock> CODEC = simpleCodec(SolidFluidReactorBlock::new);

    public SolidFluidReactorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SolidFluidReactorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, Hayo.BlockEntityTypes.SOLID_FLUID_REACTOR, world.isClientSide() ? null : SolidFluidReactorBlockEntity::tickServer);
    }
}
