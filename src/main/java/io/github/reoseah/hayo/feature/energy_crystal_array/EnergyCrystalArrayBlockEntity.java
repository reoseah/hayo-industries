package io.github.reoseah.hayo.feature.energy_crystal_array;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.api.energy.ElectricItems;
import io.github.reoseah.hayo.base.block.entity.ElectricBlockEntity;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
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

    @Getter
    protected int energyPerTick;
    @Getter
    protected float averageEnergyPerTick;

    public EnergyCrystalArrayBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.ENERGY_CRYSTAL_ARRAY, pos, state);
    }

    @SuppressWarnings("unused")
    public static void tickServer(Level level, BlockPos pos, BlockState state, EnergyCrystalArrayBlockEntity entity) {
        int chargeable = Math.min(CAPACITY - entity.storedEnergy, TRANSFER_RATE);
        if (chargeable != 0) {
            int charge = ElectricItems.tryDischarge(chargeable, entity, 0);
            if (charge > 0) {
                entity.storedEnergy += charge;
                entity.energyPerTick += charge;
                entity.setChanged();
            }
        }
        int discharge = ElectricItems.tryCharge(TRANSFER_RATE, entity, 1);
        if (discharge > 0) {
            entity.storedEnergy -= discharge;
            entity.energyPerTick -= discharge;
            entity.setChanged();
        }

        entity.averageEnergyPerTick = Mth.lerp(0.05F, entity.averageEnergyPerTick, entity.energyPerTick);
        entity.energyPerTick = 0;
    }

    public static int receiveEnergy(int amount, ServerLevel level, BlockPos pos, Direction side, EnergyCrystalArrayBlockEntity entity) {
        int change = Math.min(amount, Math.min(CAPACITY - entity.getStoredEnergy(), TRANSFER_RATE));
        entity.storedEnergy += change;
        entity.energyPerTick += change;
        return change;
    }

    @Override
    protected NonNullList<ItemStack> createInventory() {
        return NonNullList.withSize(2, ItemStack.EMPTY);
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayo.energy_crystal_array");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int menuId, Inventory playerInventory, Player player) {
        return new EnergyCrystalArrayMenu(menuId, this, playerInventory);
    }
}
