package hayo.energy_storage;

import hayo.Hayo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class CrystalEnergyStorageBlockEntity extends EnergyStorageBlockEntity {
    public static final int CAPACITY = 400_000;
    public static final int TRANSFER_LIMIT = 128;

    public CrystalEnergyStorageBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.CRYSTAL_ENERGY_STORAGE, pos, state);
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayo.crystal_energy_storage");
    }

    @Override
    protected int getEnergyCapacity() {
        return CAPACITY;
    }

    @Override
    protected int getEnergyTransferLimit() {
        return TRANSFER_LIMIT;
    }
}