package hayo.features.energy_storage;

import hayo.Hayo;
import hayo.HayoConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class MfsuBlockEntity extends EnergyStorageBlockEntity {
    public MfsuBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.MFSU, pos, state);
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayo.mfsu");
    }

    @Override
    public int getEnergyCapacity() {
        return HayoConfig.instance().energyStorage().mfsuCapacity();
    }

    @Override
    protected int getEnergyTransferLimit() {
        return HayoConfig.instance().energyStorage().mfsuTransferRate();
    }
}
