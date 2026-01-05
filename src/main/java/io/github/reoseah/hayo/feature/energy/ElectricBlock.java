package io.github.reoseah.hayo.feature.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/// Base interface for blocks that interact with energy.
///
/// Most blocks implement it through [ElectricSenderBlock],
/// [ElectricReceiverBlock] or [ElectricCableBlock], this type
/// itself functions more like a "marker" interface.
///
/// @see ElectricBlocks
public interface ElectricBlock {
    default boolean connectsToCables(BlockState state, LevelReader level, BlockPos pos, Direction side) {
        return true;
    }
}
