package io.github.reoseah.hayo.feature.electric_beacon;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.base.block.entity.HayoContainerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class ElectricBeaconBlockEntity extends HayoContainerBlockEntity {
    public @Nullable ElectricBeaconOption tier1Choice;
    public @Nullable ElectricBeaconOption tier2Choice;
    public @Nullable ElectricBeaconOption tier3Choice;
    public @Nullable ElectricBeaconOption tier4Choice;

    public ElectricBeaconBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.ELECTRIC_BEACON, pos, state);
    }

    @Override
    protected NonNullList<ItemStack> createInventory() {
        return NonNullList.create();
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.hayo.electric_beacon");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ElectricBeaconMenu(containerId, this, inventory);
    }
}
