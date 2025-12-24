package io.github.reoseah.hayo.api.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public interface ElectricBlock {
    default boolean connectsToCables(BlockState state, LevelReader level, BlockPos pos, Direction side) {
        return true;
    }
}
