package hayo.processing_machine.classic;

import hayo.common.menu.HayoContainerMenu;
import hayo.common.menuslot.ResultSlot;
import hayo.common.menuslot.TagFilteredSlot;
import hayo.energy.item.EnergyComponents;
import hayo.processing_machine.MachineContainerData;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import org.jspecify.annotations.Nullable;

public abstract class ClassicMachineMenu extends HayoContainerMenu {
    public final MachineContainerData machineData;

    protected ClassicMachineMenu(@Nullable MenuType<?> menuType, int containerId, Container container, MachineContainerData machineData, Inventory inventory) {
        super(menuType, containerId, container);

        this.machineData = machineData;
        this.addDataSlots(this.machineData);

        this.addSlot(new Slot(container, ClassicMachineBlockEntity.INPUT, 47, 18));
        this.addSlot(new Slot(container, ClassicMachineBlockEntity.BATTERY, 47, 54));
        this.addSlot(new ResultSlot(container, ClassicMachineBlockEntity.OUTPUT, 107, 36));
        this.addSlot(new TagFilteredSlot(container, 3, 152, 8, this.getUpgradeTag()));
        this.addSlot(new TagFilteredSlot(container, 4, 152, 26, this.getUpgradeTag()));
        this.addSlot(new TagFilteredSlot(container, 5, 152, 44, this.getUpgradeTag()));
        this.addSlot(new TagFilteredSlot(container, 6, 152, 62, this.getUpgradeTag()));
        this.addStandardInventorySlots(inventory, 8, 84);
    }

    protected abstract TagKey<Item> getUpgradeTag();

    protected abstract RecipeType<? extends Recipe<SingleRecipeInput>> getRecipeType();

    @Override
    protected boolean handleQuickMoveFromInventory(ItemStack stack, Player player, int index) {
        if (stack.is(this.getUpgradeTag())) {
            return this.moveItemStackTo(stack, 3, 7, false);
        }
        if (player.level().recipeAccess().getSynchronizedRecipes().getFirstMatch(this.getRecipeType(), new SingleRecipeInput(stack), player.level()).isPresent()) {
            return this.moveItemStackTo(stack, 0, 1, false);
        }
        if (EnergyComponents.chargesBlocks(stack)) {
            return this.moveItemStackTo(stack, 1, 2, false);
        }
        return false;
    }
}
