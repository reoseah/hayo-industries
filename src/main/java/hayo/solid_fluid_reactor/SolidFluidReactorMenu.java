package hayo.solid_fluid_reactor;

import hayo.Hayo;
import hayo.common.menu.HayoContainerMenu;
import hayo.common.menuslot.ResultSlot;
import hayo.fluid_stack.ItemFluidRecipeInput;
import hayo.processing_machine.classic.MachineUpgradeSlot;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;

public class SolidFluidReactorMenu extends HayoContainerMenu {
    public final SolidFluidReactorContainerData data;

    public SolidFluidReactorMenu(int containerId, Inventory inventory) {
        this(containerId, new SimpleContainer(SolidFluidReactorBlockEntity.SLOTS), new SolidFluidReactorContainerData.Clientside(), inventory);
    }

    public SolidFluidReactorMenu(int containerId, SolidFluidReactorBlockEntity entity, Inventory inventory) {
        this(containerId, entity, new SolidFluidReactorContainerData.Serverside(entity), inventory);
    }

    public SolidFluidReactorMenu(int containerId, Container container, SolidFluidReactorContainerData data, Inventory inventory) {
        super(Hayo.MenuTypes.SOLID_FLUID_REACTOR, containerId, container);

        this.data = data;
        this.addDataSlots(data);

        this.addSlot(new Slot(container, 0, 62, 17));
        this.addSlot(new Slot(container, 1, 8, 17));
        this.addSlot(new Slot(container, 2, 170, 17));
        this.addSlot(new Slot(container, 3, 62, 53));
        this.addSlot(new ResultSlot(container, 4, 116, 17));
        this.addSlot(new ResultSlot(container, 5, 116, 35));
        this.addSlot(new ResultSlot(container, 6, 116, 53));
        this.addSlot(new ResultSlot(container, 7, 8, 53));
        this.addSlot(new ResultSlot(container, 8, 170, 53));
        for (int i = 0; i < 4; i++) {
            this.addSlot(new MachineUpgradeSlot(container, 9 + i, 206, 8 + 18 * i, Hayo.ItemTags.SOLID_FLUID_REACTOR_UPGRADES, 9, 4));
        }

        this.addStandardInventorySlots(inventory, 35, 84);
    }

    @Override
    protected boolean handleQuickMoveFromInventory(ItemStack stack, Player player, int index) {
        if (stack.is(Hayo.ItemTags.SOLID_FLUID_REACTOR_UPGRADES)) {
            return this.moveItemStackTo(stack, SolidFluidReactorBlockEntity.UPGRADE_1, SolidFluidReactorBlockEntity.UPGRADE_1 + SolidFluidReactorBlockEntity.UPGRADES, false);
        }

        if (player.level()
                .recipeAccess()
                .getSynchronizedRecipes()
                .getFirstMatch(Hayo.RecipeTypes.SOLID_FLUID_REACTING, new ItemFluidRecipeInput(stack, this.data.inputFluid()), player.level())
                .isPresent()) {
            return this.moveItemStackTo(stack, SolidFluidReactorBlockEntity.INPUT, SolidFluidReactorBlockEntity.INPUT + 1, false);
        }
        if (player.level()
                .recipeAccess()
                .getSynchronizedRecipes()
                .getFirstMatch(Hayo.RecipeTypes.FLUID_DRAINING, new SingleRecipeInput(stack), player.level())
                .isPresent()) {
            return this.moveItemStackTo(stack, SolidFluidReactorBlockEntity.INPUT_TANK_INPUT, SolidFluidReactorBlockEntity.INPUT_TANK_INPUT + 1, false);
        }
        if (player.level()
                .recipeAccess()
                .getSynchronizedRecipes()
                .getFirstMatch(Hayo.RecipeTypes.FLUID_FILLING, new ItemFluidRecipeInput(stack, this.data.resultFluid()), player.level())
                .isPresent()) {
            return this.moveItemStackTo(stack, SolidFluidReactorBlockEntity.OUTPUT_TANK_INPUT, SolidFluidReactorBlockEntity.OUTPUT_TANK_INPUT + 1, false);
        }

        return false;
    }
}
