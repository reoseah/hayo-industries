package hayo.features.processing_machine.classic;

import hayo.Hayo;
import hayo.common.block.HorizontalDirectionalElectricalBlock;
import hayo.energy.block.EnergyReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class CompressorBlock extends HorizontalDirectionalElectricalBlock implements EnergyReceiver {

    public CompressorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CompressorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, Hayo.BlockEntityTypes.COMPRESSOR, world.isClientSide() ? null : CompressorBlockEntity::tickServer);
    }
}
