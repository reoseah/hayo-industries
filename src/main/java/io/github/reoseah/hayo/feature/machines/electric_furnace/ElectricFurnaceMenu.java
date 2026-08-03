package io.github.reoseah.hayo.feature.machines.electric_furnace;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.machines.ClassicMachineBlockEntity;
import io.github.reoseah.hayo.feature.machines.MachineMenu;
import io.github.reoseah.hayo.feature.machines.ResultSlot;
import io.github.reoseah.hayo.feature.machines.TagFilteredSlot;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipePropertySet;

import java.util.Map;

public class ElectricFurnaceMenu extends MachineMenu {
    private final DataSlot recipeMode;
    private final Map<ElectricFurnaceMode, RecipePropertySet> recipeInputs;

    public ElectricFurnaceMenu(int menuId, Inventory inventory) {
        this(menuId, new SimpleContainer(ClassicMachineBlockEntity.SLOTS), createData(), inventory, DataSlot.standalone());
    }

    public ElectricFurnaceMenu(int menuId, ElectricFurnaceBlockEntity entity, Inventory inventory) {
        this(menuId, entity, createData(entity), inventory, createRecipeModeData(entity));
    }

    protected ElectricFurnaceMenu(int menuId, Container container, ContainerData data, Inventory inventory, DataSlot recipeMode) {
        super(Hayo.MenuTypes.ELECTRIC_FURNACE, menuId, container, data, inventory);

        this.addSlot(new Slot(container, 0, 47, 18));
        this.addSlot(new Slot(container, 1, 47, 54));
        this.addSlot(new ResultSlot(container, 2, 107, 36));

        this.addSlot(new ElectricFurnaceUpgradeSlot(container, 3, 152, 8));
        this.addSlot(new ElectricFurnaceUpgradeSlot(container, 4, 152, 26));
        this.addSlot(new ElectricFurnaceUpgradeSlot(container, 5, 152, 44));
        this.addSlot(new ElectricFurnaceUpgradeSlot(container, 6, 152, 62));

        this.addStandardInventorySlots(inventory, 8, 84);

        this.recipeMode = this.addDataSlot(recipeMode);

        var level = inventory.player.level();
        this.recipeInputs = Map.of( //
                ElectricFurnaceMode.SMELTING, level.recipeAccess().propertySet(RecipePropertySet.FURNACE_INPUT), //
                ElectricFurnaceMode.BLASTING, level.recipeAccess().propertySet(RecipePropertySet.BLAST_FURNACE_INPUT), //
                ElectricFurnaceMode.SMOKING, level.recipeAccess().propertySet(RecipePropertySet.SMOKER_INPUT));
    }

    public static DataSlot createRecipeModeData(ElectricFurnaceBlockEntity entity) {
        return new DataSlot() {
            @Override
            public int get() {
                return entity.getMode().ordinal();
            }

            @Override
            public void set(int value) {
            }
        };
    }

    public ElectricFurnaceMode getRecipeMode() {
        return ElectricFurnaceMode.values()[Mth.clamp(this.recipeMode.get(), 0, 2)];
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveClassicMachineStack(this, player, index, 1, 1, 1, 4, stack -> this.recipeInputs.get(this.getRecipeMode()).test(stack), Hayo.ItemTags.ELECTRIC_FURNACE_UPGRADES);
    }

    public static class ElectricFurnaceUpgradeSlot extends TagFilteredSlot {
        public ElectricFurnaceUpgradeSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y, Hayo.ItemTags.ELECTRIC_FURNACE_UPGRADES);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (!super.mayPlace(stack)) return false;

            if (stack.is(Hayo.Items.BLASTING_UPGRADE) || stack.is(Hayo.Items.SMOKING_UPGRADE)) {
                for (int i = ClassicMachineBlockEntity.FIRST_UPGRADE_SLOT; i <= ClassicMachineBlockEntity.LAST_UPGRADE_SLOT; i++) {
                    if (i == this.index) continue;

                    var upgrade = this.container.getItem(i);
                    if (upgrade.is(Hayo.Items.BLASTING_UPGRADE) || upgrade.is(Hayo.Items.SMOKING_UPGRADE)) {
                        return false;
                    }
                }
            }

            if (stack.is(Hayo.Items.STREAMLINE_OVERHAUL_UPGRADE)) {
                for (int i = ClassicMachineBlockEntity.FIRST_UPGRADE_SLOT; i <= ClassicMachineBlockEntity.LAST_UPGRADE_SLOT; i++) {
                    if (i == this.index) continue;

                    var upgrade = this.container.getItem(i);
                    if (upgrade.is(Hayo.Items.STREAMLINE_OVERHAUL_UPGRADE)) {
                        return false;
                    }
                }
            }

            return true;
        }
    }
}
