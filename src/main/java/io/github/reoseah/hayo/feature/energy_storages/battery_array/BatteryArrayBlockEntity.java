package io.github.reoseah.hayo.feature.energy_storages.battery_array;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.energy_storages.EnergyStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BatteryArrayBlockEntity extends EnergyStorageBlockEntity implements MenuProvider {
    public static final int CAPACITY = 40_000;
    public static final int TRANSFER_RATE = 32;

    public BatteryArrayBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.BATTERY_ARRAY, pos, state);
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayo.battery_array");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int menuId, Inventory playerInventory, Player player) {
        return new BatteryArrayMenu(menuId, this, playerInventory);
    }

    @Override
    protected int getEnergyCapacity() {
        return CAPACITY;
    }

    @Override
    protected int getEnergyTransferRate() {
        return TRANSFER_RATE;
    }
}
