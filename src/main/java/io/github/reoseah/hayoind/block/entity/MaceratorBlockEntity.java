package io.github.reoseah.hayoind.block.entity;

import io.github.reoseah.hayoind.Hayo;
import io.github.reoseah.hayoind.menu.ClassicProcessingMachineMenu;
import io.github.reoseah.hayoind.recipe.MaceratingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MaceratorBlockEntity extends ProcessingMachineBlockEntity<MaceratingRecipe, SingleRecipeInput> {
    public static final int CAPACITY = 1200;

    public MaceratorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.MACERATOR, Hayo.RecipeTypes.MACERATING, pos, state);
    }

    @Override
    protected NonNullList<ItemStack> createInventory() {
        return NonNullList.withSize(7, ItemStack.EMPTY);
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayoind.macerator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int menuId, Inventory inventory, Player player) {
        return new ClassicProcessingMachineMenu.MaceratorMenu(menuId, this, inventory);
    }
}
