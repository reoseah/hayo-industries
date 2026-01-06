package io.github.reoseah.hayo.feature.processing_machines.extractor;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.processing_machines.MachineMenu;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;

public class ExtractorMenu extends MachineMenu {
    public ExtractorMenu(int menuId, Inventory inventory) {
        this(menuId, new SimpleContainer(ExtractorBlockEntity.SLOTS), createData(), inventory);
    }

    public ExtractorMenu(int menuId, ExtractorBlockEntity entity, Inventory inventory) {
        this(menuId, entity, createData(entity), inventory);
    }

    protected ExtractorMenu(int menuId, Container container, ContainerData data, Inventory inventory) {
        super(Hayo.MenuTypes.EXTRACTOR, menuId, container, data, inventory);

        addClassicSlots(this, container, inventory, Hayo.ItemTags.EXTRACTOR_UPGRADES);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveClassicMachineStack(this, player, index, 1, 1, 1, 4, this::isRecipeInput, Hayo.ItemTags.EXTRACTOR_UPGRADES);
    }

    protected boolean isRecipeInput(ItemStack stack) {
        // TODO: synchronize recipe inputs, quick move only valid inputs
        return true;
    }

    @Override
    public int getEnergyUseRate() {
        return ExtractorBlockEntity.ENERGY_USE_RATE * (1 + this.getOverclockCount());
    }
}
