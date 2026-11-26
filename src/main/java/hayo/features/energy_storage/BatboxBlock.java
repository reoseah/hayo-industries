package hayo.features.energy_storage;

import hayo.Hayo;
import hayo.common.block.DirectionalElectricalBlock;
import hayo.energy.block.EnergyReceiver;
import hayo.energy.block.EnergySender;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class BatboxBlock extends DirectionalElectricalBlock implements EnergyReceiver, EnergySender {
    public BatboxBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BatboxBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, Hayo.BlockEntityTypes.BATBOX, world.isClientSide() ? null : EnergyStorageBlockEntity::tickServer);
    }

    @Override
    public boolean canReceiveEnergy(BlockState state, ServerLevel level, BlockPos pos, Direction side) {
        return side.getOpposite() != state.getValue(FACING);
    }
}
