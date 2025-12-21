package io.github.reoseah.hayo.feature.processing_machines.extractor;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.processing_machines.MachineBlockEntity;
import io.github.reoseah.hayo.feature.processing_machines.MachineMenu;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class ExtractorMenu extends MachineMenu {
    public static final int SLOTS = ExtractorBlockEntity.SLOTS;

    public ExtractorMenu(int menuId, Inventory inventory) {
        this(menuId, new SimpleContainer(SLOTS), new SimpleContainerData(7), inventory);
    }

    public ExtractorMenu(int menuId, ExtractorBlockEntity entity, Inventory inventory) {
        this(menuId, entity, MachineBlockEntity.createData(entity), inventory);
    }

    protected ExtractorMenu(int menuId, Container container, ContainerData data, Inventory inventory) {
        super(Hayo.MenuTypes.EXTRACTOR, Hayo.ItemTags.EXTRACTOR_UPGRADES, menuId, container, data, inventory);

        addClassicSlots(this, container, inventory);
    }

    @Override
    protected int getFirstPlayerSlot() {
        return SLOTS;
    }

    @Override
    protected boolean isRecipeInput(ItemStack stack) {
        // TODO: synchronize recipe inputs, quick move only valid inputs
        return true;
    }

    @Override
    public int getEnergyUseRate() {
        return ExtractorBlockEntity.ENERGY_USE_RATE * (1 + this.getOverclockCount());
    }
}
