package io.github.reoseah.hayoind.block.entity;

import io.github.reoseah.hayoind.Hayo;
import io.github.reoseah.hayoind.item.ElectricItems;
import io.github.reoseah.hayoind.menu.EnergyCrystalArrayMenu;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class EnergyCrystalArrayBlockEntity extends HayoElectricBlockEntity implements MenuProvider {
    public static final int CAPACITY = 1_000_000;
    public static final int TRANSFER_RATE = 128;

    @Getter
    protected int energyPerTick;
    @Getter
    protected float averageEnergyPerTick;

    public EnergyCrystalArrayBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.ENERGY_CRYSTAL_ARRAY, pos, state);
    }

    @Override
    protected NonNullList<ItemStack> createInventory() {
        return NonNullList.withSize(2, ItemStack.EMPTY);
    }

    @Override
    public Component getName() {
        return this.customName != null ? this.customName : Component.translatable("block.hayoind.energy_crystal_array");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int menuId, Inventory playerInventory, Player player) {
        return new EnergyCrystalArrayMenu(menuId, this, playerInventory);
    }

    @SuppressWarnings("unused")
    public static void tickServer(Level level, BlockPos pos, BlockState state, EnergyCrystalArrayBlockEntity entity) {
        entity.energyPerTick = 0;
        int chargeable = Math.min(CAPACITY - entity.storedEnergy, TRANSFER_RATE);
        if (chargeable != 0) {
            int charge = ElectricItems.tryDischarge(chargeable, entity.getItem(0), stack -> entity.setItem(0, stack));
            if (charge > 0) {
                entity.storedEnergy += charge;
                entity.energyPerTick += charge;
                entity.setChanged();
            }
        }
        int discharge = ElectricItems.tryCharge(TRANSFER_RATE, entity.getItem(1), stack -> entity.setItem(1, stack));
        if (discharge > 0) {
            entity.storedEnergy -= discharge;
            entity.energyPerTick -= discharge;
            entity.setChanged();
        }

        entity.averageEnergyPerTick = Mth.lerp(0.05F, entity.averageEnergyPerTick, entity.energyPerTick);
    }
}
