package hayo.features.energy_storage;

import hayo.Hayo;
import hayo.HayoConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class BatboxBlockEntity extends EnergyStorageBlockEntity {
    public BatboxBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.BATBOX, pos, state);
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayo.batbox");
    }

    @Override
    public int getEnergyCapacity() {
        return HayoConfig.instance().energyStorage().batboxCapacity();
    }

    @Override
    protected int getEnergyTransferLimit() {
        return HayoConfig.instance().energyStorage().batboxTransferRate();
    }
}