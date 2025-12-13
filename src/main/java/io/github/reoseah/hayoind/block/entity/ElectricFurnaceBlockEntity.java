package io.github.reoseah.hayoind.block.entity;

import io.github.reoseah.hayoind.Hayo;
import io.github.reoseah.hayoind.item.ElectricItems;
import io.github.reoseah.hayoind.menu.ElectricFurnaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ElectricFurnaceBlockEntity extends HayoElectricBlockEntity {
    public static final int CAPACITY = 400;
    public static final int TRANSFER_RATE = 32;
    public static final int BATTERY_SLOT = 1;

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.ELECTRIC_FURNACE, pos, state);
    }

    @Override
    protected NonNullList<ItemStack> createInventory() {
        return NonNullList.withSize(7, ItemStack.EMPTY);
    }

    @Override
    public Component getName() {
        return this.customName != null ? this.customName : Component.translatable("block.hayoind.electric_furnace");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int menuId, Inventory inventory, Player player) {
        return new ElectricFurnaceMenu(menuId, this, inventory);
    }

    @SuppressWarnings("unused")
    public static void tickServer(Level level, BlockPos pos, BlockState state, ElectricFurnaceBlockEntity entity) {
        int chargeable = Math.min(CAPACITY - entity.storedEnergy, TRANSFER_RATE);
        if (chargeable != 0) {
            int charge = ElectricItems.tryDischarge(chargeable, entity, BATTERY_SLOT);
            if (charge > 0) {
                entity.storedEnergy += charge;
                entity.setChanged();
            }
        }
    }
}
