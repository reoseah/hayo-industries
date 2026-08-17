package hayo.energy.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public interface ElectricReceiverBlock extends ElectricBlock {
    default boolean canReceiveEnergy(BlockState state, ServerLevel level, BlockPos pos, Direction side) {
        return this.connectsToCables(state, level, pos, side);
    }

    int getReceivableEnergy(ServerLevel level, BlockPos pos, Direction side);

    int receiveEnergy(int amount, ServerLevel level, BlockPos pos, Direction side);
}
