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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class EnergyCrystalArrayBlockEntity extends HayoContainerBlockEntity implements MenuProvider {
    public static final int CAPACITY = 1_000_000;
    public static final int TRANSFER_RATE = 128;

    @Getter
    protected int storedEnergy;
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
    public Component getDisplayName() {
        return this.getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int menuId, Inventory playerInventory, Player player) {
        return new EnergyCrystalArrayMenu(menuId, this, playerInventory);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        view.putInt("StoredEnergy", this.storedEnergy);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        this.storedEnergy = view.getIntOr("StoredEnergy", 0);
    }

    @SuppressWarnings("unused")
    public static void tickServer(Level level, BlockPos pos, BlockState state, EnergyCrystalArrayBlockEntity entity) {
        entity.energyPerTick = 0;
        int vacant = Math.min(CAPACITY - entity.storedEnergy, TRANSFER_RATE);
        if (vacant != 0) {
            int charge = ElectricItems.tryDischarge(vacant, entity.getItem(0), stack -> entity.setItem(0, stack));
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
