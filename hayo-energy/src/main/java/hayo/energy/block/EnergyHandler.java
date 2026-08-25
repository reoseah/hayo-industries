package hayo.energy.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/// Base interface for blocks that interact with energy.
///
/// Most blocks implement it through [EnergySender],
/// [EnergyReceiver] or [EnergyTransferrer].
public interface EnergyHandler {
    default boolean connectsToCables(BlockState state, LevelReader level, BlockPos pos, Direction direction) {
        return true;
    }
}
