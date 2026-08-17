package hayo.energy.block;

import net.minecraft.world.level.block.state.BlockState;

public interface ElectricCableBlock extends ElectricBlock {
    int getTransferLimit(BlockState state);
}
