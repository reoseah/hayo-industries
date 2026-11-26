package hayo.features.energy_storage;

import hayo.Hayo;
import hayo.HayoConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class MfeBlockEntity extends EnergyStorageBlockEntity {
    public MfeBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.MFE, pos, state);
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayo.mfe");
    }

    @Override
    public int getEnergyCapacity() {
        return HayoConfig.instance().energyStorage().mfeCapacity();
    }

    @Override
    protected int getEnergyTransferLimit() {
        return HayoConfig.instance().energyStorage().mfeTransferRate();
    }
}