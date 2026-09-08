package hayo.processing_machine.classic;

import hayo.common.menu.HayoContainerMenu;
import hayo.common.menuslot.ResultSlot;
import hayo.energy.item.EnergyComponents;
import hayo.processing_machine.MachineContainerData;
import hayo.processing_machine.UpgradableMachineBlockEntity;
import hayo.processing_machine.UpgradableMachineContainerData;
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
    public final UpgradableMachineContainerData upgradeData;

    protected ClassicMachineMenu(@Nullable MenuType<?> menuType, int containerId, Container container, MachineContainerData machineData, UpgradableMachineContainerData upgradeData, Inventory inventory) {
        super(menuType, containerId, container);

        this.machineData = machineData;
        this.addDataSlots(this.machineData);

        this.upgradeData = upgradeData;
        this.addDataSlots(this.upgradeData);

        this.addSlot(new Slot(container, 0, 47, 18));
        this.addSlot(new Slot(container, 1, 47, 54));
        this.addSlot(new ResultSlot(container, 2, 107, 36));
        this.addSlot(new MachineUpgradeSlot(container, 3, 152, 8, this.getUpgradeTag(), 3, 4));
        this.addSlot(new MachineUpgradeSlot(container, 4, 152, 26, this.getUpgradeTag(), 3, 4));
        this.addSlot(new MachineUpgradeSlot(container, 5, 152, 44, this.getUpgradeTag(), 3, 4));
        this.addSlot(new MachineUpgradeSlot(container, 6, 152, 62, this.getUpgradeTag(), 3, 4));
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

    public int getRecipeProgressPerTick() {
        int energyUse = this.machineData.energyUseRate();
        if (this.upgradeData.hasInductionUpgrade()) {
            return 1 + (energyUse - 1) * this.upgradeData.inductionHeat() / UpgradableMachineBlockEntity.MAX_INDUCTION_HEAT;
        }
        return energyUse;
    }
}
