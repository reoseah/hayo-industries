package io.github.reoseah.hayo.feature.processing_machines.matter_generator;

import com.google.common.base.Predicates;
import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.processing_machines.MachineMenu;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class MatterGeneratorMenu extends MachineMenu {
    public final List<RecipeHolder<MatterGeneratingRecipe>> recipes;
    public final DataSlot selectedIdx;

    public MatterGeneratorMenu(int menuId, Inventory inventory) {
        this(menuId, new SimpleContainer(MatterGeneratorBlockEntity.SLOTS), createData(), inventory);
    }

    public MatterGeneratorMenu(int menuId, MatterGeneratorBlockEntity entity, Inventory inventory) {
        this(menuId, entity, createData(entity), inventory);
    }

    protected MatterGeneratorMenu(int menuId, Container container, ContainerData data, Inventory inventory) {
        super(Hayo.MenuTypes.MATTER_GENERATOR, menuId, container, data, inventory);

        addMatterGeneratorSlots(this, container, inventory);

        var level = inventory.player.level();
        this.recipes = List.copyOf(level.recipeAccess().getSynchronizedRecipes().getAllOfType(Hayo.RecipeTypes.MATTER_GENERATING));

        this.selectedIdx = (container instanceof MatterGeneratorBlockEntity entity) ? createSelectedIdx(entity, this.recipes) : DataSlot.standalone();
        this.addDataSlot(this.selectedIdx);
    }

    private static DataSlot createSelectedIdx(MatterGeneratorBlockEntity entity, List<RecipeHolder<MatterGeneratingRecipe>> recipes) {
        var ids = recipes.stream().map(holder -> holder.id().identifier()).toList();

        return new DataSlot() {
            @Override
            public int get() {
                return ids.indexOf(entity.selectedRecipeId);
            }

            @Override
            public void set(int value) {
                entity.selectRecipe(value >= 0 && value < ids.size() ? ids.get(value) : null);
            }
        };
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveClassicMachineStack(this, player, index, 0, 1, 1, 0, Predicates.alwaysFalse(), null);
    }

    @Override
    public int getEnergyUseRate() {
        return MatterGeneratorBlockEntity.TRANSFER_RATE;
    }

    public int getSelectedRecipeIdx() {
        return this.selectedIdx.get();
    }

    public RecipeHolder<MatterGeneratingRecipe> getSelectedRecipe() {
        return this.recipes.get(this.getSelectedRecipeIdx());
    }

    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId >= 0 && buttonId < this.recipes.size()) {
            this.selectedIdx.set(buttonId);

            return true;
        }

        return super.clickMenuButton(player, buttonId);
    }
}
