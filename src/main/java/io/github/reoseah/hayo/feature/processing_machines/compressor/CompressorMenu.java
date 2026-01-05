package io.github.reoseah.hayo.feature.processing_machines.compressor;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.processing_machines.MachineBlockEntity;
import io.github.reoseah.hayo.feature.processing_machines.MachineMenu;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class CompressorMenu extends MachineMenu {
    public CompressorMenu(int menuId, Inventory inventory) {
        this(menuId, new SimpleContainer(CompressorBlockEntity.SLOTS), new SimpleContainerData(7), inventory);
    }

    public CompressorMenu(int menuId, CompressorBlockEntity entity, Inventory inventory) {
        this(menuId, entity, MachineBlockEntity.createData(entity), inventory);
    }

    protected CompressorMenu(int menuId, Container container, ContainerData data, Inventory inventory) {
        super(Hayo.MenuTypes.COMPRESSOR, menuId, container, data, inventory);

        addClassicSlots(this, container, inventory, Hayo.ItemTags.COMPRESSOR_UPGRADES);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveClassicMachineStack(this, player, index, 1, 1, 1, 4, this::isRecipeInput, Hayo.ItemTags.COMPRESSOR_UPGRADES);
    }

    protected boolean isRecipeInput(ItemStack stack) {
        // TODO: synchronize recipe inputs, quick move only valid inputs
        return true;
    }

    @Override
    public int getEnergyUseRate() {
        return CompressorBlockEntity.ENERGY_USE_RATE * (1 + this.getOverclockCount());
    }
}
