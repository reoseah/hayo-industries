package io.github.reoseah.hayo.feature.energy_storages;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.api.energy.ElectricItems;
import io.github.reoseah.hayo.base.block.entity.ElectricBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class EnergyCrystalArrayBlockEntity extends ElectricBlockEntity implements MenuProvider {
    public static final int CAPACITY = 1_000_000;
    public static final int TRANSFER_RATE = 128;

    public EnergyCrystalArrayBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.ENERGY_CRYSTAL_ARRAY, pos, state);
    }

    @SuppressWarnings("unused")
    public static void tickServer(Level level, BlockPos pos, BlockState state, EnergyCrystalArrayBlockEntity entity) {
        tickChargeFromSlot(entity, 0, CAPACITY, TRANSFER_RATE);
        int discharge = ElectricItems.tryCharge(TRANSFER_RATE, entity, 1);
        if (discharge > 0) {
            entity.storedEnergy -= discharge;
            entity.energyPerTick -= discharge;
            entity.setChanged();
        }
        tickEnergyPerTick(entity);
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayo.energy_crystal_array");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int menuId, Inventory playerInventory, Player player) {
        return new EnergyCrystalArrayMenu(menuId, this, playerInventory);
    }

    @Override
    protected NonNullList<ItemStack> createInventory() {
        return NonNullList.withSize(2, ItemStack.EMPTY);
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
