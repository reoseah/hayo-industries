package io.github.reoseah.hayo.feature.energy;

import io.github.reoseah.hayo.base.block.entity.ElectricBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public interface ElectricReceiverBlock extends ElectricBlock {
    default boolean canReceiveEnergy(BlockState state, ServerLevel level, BlockPos pos, Direction side) {
        return this.connectsToCables(state, level, pos, side);
    }

    default int getReceivableEnergy(ServerLevel level, BlockPos pos, Direction side) {
        if (level.getBlockEntity(pos) instanceof ElectricBlockEntity entity) {
            return entity.getReceivableEnergy();
        }
        return 0;
    }

    default int receiveEnergy(int amount, ServerLevel level, BlockPos pos, Direction side) {
        if (level.getBlockEntity(pos) instanceof ElectricBlockEntity entity) {
            return entity.receiveEnergy(amount);
        }
        return 0;
    }
}
