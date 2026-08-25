package hayo.energy.block;

import net.minecraft.world.level.block.state.BlockState;

public interface EnergyTransferrer extends EnergyHandler {
    int getTransferLimit(BlockState state);
}
