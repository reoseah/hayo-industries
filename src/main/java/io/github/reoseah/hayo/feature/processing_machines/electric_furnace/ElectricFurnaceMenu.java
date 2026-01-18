package io.github.reoseah.hayo.feature.processing_machines.electric_furnace;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.processing_machines.MachineMenu;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipePropertySet;

import java.util.Map;

public class ElectricFurnaceMenu extends MachineMenu {
    private final DataSlot recipeMode;
    private final Map<ElectricFurnaceMode, RecipePropertySet> recipeInputs;

    public ElectricFurnaceMenu(int menuId, Inventory inventory) {
        this(menuId, new SimpleContainer(ElectricFurnaceBlockEntity.SLOTS), createData(), inventory, DataSlot.standalone());
    }

    public ElectricFurnaceMenu(int menuId, ElectricFurnaceBlockEntity entity, Inventory inventory) {
        this(menuId, entity, createData(entity), inventory, createRecipeModeData(entity));
    }

    protected ElectricFurnaceMenu(int menuId, Container container, ContainerData data, Inventory inventory, DataSlot recipeMode) {
        super(Hayo.MenuTypes.ELECTRIC_FURNACE, menuId, container, data, inventory);

        addClassicSlots(this, container, inventory, Hayo.ItemTags.ELECTRIC_FURNACE_UPGRADES);

        this.recipeMode = this.addDataSlot(recipeMode);

        var level = inventory.player.level();
        this.recipeInputs = Map.of( //
                ElectricFurnaceMode.NORMAL, level.recipeAccess().propertySet(RecipePropertySet.FURNACE_INPUT), //
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
        return quickMoveClassicMachineStack(this, player, index, 1, 1, 1, 4, this::isRecipeInput, Hayo.ItemTags.ELECTRIC_FURNACE_UPGRADES);
    }

    protected boolean isRecipeInput(ItemStack stack) {
        return this.recipeInputs.get(this.getRecipeMode()).test(stack);
    }
}
