package hayo.energy.block;

import net.minecraft.world.level.block.state.BlockState;

public non-sealed interface EnergyCable extends BaseEnergyBlock {
    int getTransferLimit(BlockState state);
}
