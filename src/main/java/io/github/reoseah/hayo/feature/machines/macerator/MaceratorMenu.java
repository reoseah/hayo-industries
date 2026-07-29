package io.github.reoseah.hayo.feature.machines.macerator;

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

public class MaceratorMenu extends MachineMenu {
    public MaceratorMenu(int menuId, Inventory inventory) {
        this(menuId, new SimpleContainer(ClassicMachineBlockEntity.SLOTS), createData(), inventory);
    }

    public MaceratorMenu(int menuId, MaceratorBlockEntity entity, Inventory inventory) {
        this(menuId, entity, createData(entity), inventory);
    }

    protected MaceratorMenu(int menuId, Container container, ContainerData data, Inventory inventory) {
        super(Hayo.MenuTypes.MACERATOR, menuId, container, data, inventory);

        addClassicSlots(this, container, inventory, Hayo.ItemTags.MACERATOR_UPGRADES);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveClassicMachineStack(this, player, index, 1, 1, 1, 4, //
                stack -> player.level() //
                        .recipeAccess() //
                        .getSynchronizedRecipes() //
                        .getFirstMatch(Hayo.RecipeTypes.MACERATING, new SingleRecipeInput(stack), player.level()) //
                        .isPresent(), //
                Hayo.ItemTags.MACERATOR_UPGRADES);
    }
}
