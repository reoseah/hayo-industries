package io.github.reoseah.hayo.feature.energy.blocks;

import net.minecraft.world.level.block.state.BlockState;

public interface ElectricCableBlock extends ElectricBlock {
    int getTransferLimit(BlockState state);
}
