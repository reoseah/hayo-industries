package io.github.reoseah.hayo.feature.energy_storages;

import io.github.reoseah.hayo.Hayo;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedEnergyStorageBlockEntity extends EnergyStorageBlockEntity implements ExtendedMenuProvider<BlockPos> {
    public static final int CAPACITY = 4_000_000;
    public static final int TRANSFER_LIMIT = 512;

    public AdvancedEnergyStorageBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.ADVANCED_ENERGY_STORAGE, pos, state);
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayo.advanced_energy_storage");
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
