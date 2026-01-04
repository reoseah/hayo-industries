package io.github.reoseah.hayo.feature.processing_machines.matter_generator;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.energy.ElectricItems;
import io.github.reoseah.hayo.feature.processing_machines.MachineBlockEntity;
import io.github.reoseah.hayo.feature.processing_machines.MachineMenu;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class MatterGeneratorMenu extends MachineMenu {
    public final List<RecipeHolder<MatterGeneratingRecipe>> recipes;
    public final DataSlot selectedIdx;

    public MatterGeneratorMenu(int menuId, Inventory inventory) {
        this(menuId, new SimpleContainer(MatterGeneratorBlockEntity.SLOTS), new SimpleContainerData(7), inventory);
    }

    public MatterGeneratorMenu(int menuId, MatterGeneratorBlockEntity entity, Inventory inventory) {
        this(menuId, entity, MachineBlockEntity.createData(entity), inventory);
    }

    protected MatterGeneratorMenu(int menuId, Container container, ContainerData data, Inventory inventory) {
        super(Hayo.MenuTypes.MATTER_GENERATOR, null, menuId, container, data, inventory);

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
        var slot = this.slots.get(index);
        var stack = slot.getItem();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        var remaining = stack.copy();

        int slotCount = MatterGeneratorBlockEntity.SLOTS;
        if (index < slotCount) {
            if (!this.moveItemStackTo(stack, slotCount, slotCount + 36, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, remaining);
        } else {
            if (ElectricItems.isElectric(stack)) {
                if (!this.moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < slotCount + 27) {
                if (!this.moveItemStackTo(stack, slotCount + 27, slotCount + 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(stack, slotCount, slotCount + 27, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == remaining.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return remaining;
    }

    @Override
    protected boolean isRecipeInput(ItemStack stack) {
        return false;
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
