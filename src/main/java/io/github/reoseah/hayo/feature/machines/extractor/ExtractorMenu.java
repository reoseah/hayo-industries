package io.github.reoseah.hayo.feature.machines.extractor;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.machines.ClassicMachineBlockEntity;
import io.github.reoseah.hayo.feature.machines.MachineMenu;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;

public class ExtractorMenu extends MachineMenu {
    public ExtractorMenu(int menuId, Inventory inventory) {
        this(menuId, new SimpleContainer(ClassicMachineBlockEntity.SLOTS), createData(), inventory);
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
        return quickMoveClassicMachineStack(this, player, index, 1, 1, 1, 4, //
                stack -> player.level() //
                        .recipeAccess() //
                        .getSynchronizedRecipes() //
                        .getFirstMatch(Hayo.RecipeTypes.EXTRACTING, new SingleRecipeInput(stack), player.level()) //
                        .isPresent(), //
                Hayo.ItemTags.EXTRACTOR_UPGRADES);
    }
}
