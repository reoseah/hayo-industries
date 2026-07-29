package io.github.reoseah.hayo.feature.machines.compressor;

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

public class CompressorMenu extends MachineMenu {
    public CompressorMenu(int menuId, Inventory inventory) {
        this(menuId, new SimpleContainer(ClassicMachineBlockEntity.SLOTS), createData(), inventory);
    }

    public CompressorMenu(int menuId, CompressorBlockEntity entity, Inventory inventory) {
        this(menuId, entity, createData(entity), inventory);
    }

    protected CompressorMenu(int menuId, Container container, ContainerData data, Inventory inventory) {
        super(Hayo.MenuTypes.COMPRESSOR, menuId, container, data, inventory);

        addClassicSlots(this, container, inventory, Hayo.ItemTags.COMPRESSOR_UPGRADES);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveClassicMachineStack(this, player, index, 1, 1, 1, 4, //
                stack -> player.level() //
                        .recipeAccess() //
                        .getSynchronizedRecipes() //
                        .getFirstMatch(Hayo.RecipeTypes.COMPRESSING, new SingleRecipeInput(stack), player.level()) //
                        .isPresent(), //
                Hayo.ItemTags.COMPRESSOR_UPGRADES);
    }
}
