package io.github.reoseah.hayo.feature.processing_machines.compressor;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.processing_machines.MachineBlockEntity;
import io.github.reoseah.hayo.feature.processing_machines.MachineMenu;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class CompressorMenu extends MachineMenu {
    public static final int SLOTS = CompressorBlockEntity.SLOTS;

    public CompressorMenu(int menuId, Inventory inventory) {
        this(menuId, new SimpleContainer(SLOTS), new SimpleContainerData(7), inventory);
    }

    public CompressorMenu(int menuId, CompressorBlockEntity entity, Inventory inventory) {
        this(menuId, entity, MachineBlockEntity.createData(entity), inventory);
    }

    protected CompressorMenu(int menuId, Container container, ContainerData data, Inventory inventory) {
        super(Hayo.MenuTypes.COMPRESSOR, Hayo.ItemTags.COMPRESSOR_UPGRADES, menuId, container, data, inventory);

        addClassicSlots(this, container, inventory);
    }

    @Override
    protected boolean isRecipeInput(ItemStack stack) {
        // TODO: synchronize recipe inputs, quick move only valid inputs
        return true;
    }

    @Override
    protected int getFirstPlayerSlot() {
        return SLOTS;
    }

    @Override
    public int getEnergyUseRate() {
        return CompressorBlockEntity.ENERGY_USE_RATE * (1 + this.getOverclockCount());
    }
}
