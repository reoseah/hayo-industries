package io.github.reoseah.hayo.feature.energy_storages.crystal;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.energy_storages.EnergyStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class CrystalEnergyStorageBlockEntity extends EnergyStorageBlockEntity implements MenuProvider<BlockPos> {
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
    public @Nullable AbstractContainerMenu createMenu(int menuId, Inventory playerInventory, Player player) {
        return new CrystalEnergyStorageMenu(menuId, this, playerInventory);
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