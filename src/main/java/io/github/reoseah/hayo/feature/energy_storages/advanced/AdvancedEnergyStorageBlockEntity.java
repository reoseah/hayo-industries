package io.github.reoseah.hayo.feature.energy_storages.advanced;

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

public class AdvancedEnergyStorageBlockEntity extends EnergyStorageBlockEntity implements MenuProvider {
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
    public @Nullable AbstractContainerMenu createMenu(int menuId, Inventory playerInventory, Player player) {
        return new AdvancedEnergyStorageMenu(menuId, this, playerInventory);
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
