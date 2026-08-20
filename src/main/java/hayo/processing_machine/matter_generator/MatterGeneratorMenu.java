package hayo.processing_machine.matter_generator;

import hayo.Hayo;
import hayo.processing_machine.MachineBlockEntity;
import hayo.energy.item.EnergyComponents;
import hayo.common.menuslot.ResultSlot;
import hayo.common.menu.HayoContainerMenu;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class MatterGeneratorMenu extends HayoContainerMenu {
    public final MachineBlockEntity.MachineContainerData machineData;

    public final List<RecipeHolder<MatterGeneratingRecipe>> recipes;
    public final DataSlot selectedRecipe;

    public MatterGeneratorMenu(int menuId, Inventory inventory) {
        this(menuId, new SimpleContainer(MatterGeneratorBlockEntity.SLOTS), new MachineBlockEntity.MachineContainerData.Simple(), inventory);
    }

    public MatterGeneratorMenu(int menuId, MatterGeneratorBlockEntity entity, Inventory inventory) {
        this(menuId, entity, new MachineBlockEntity.MachineContainerData.Entity(entity), inventory);
    }

    protected MatterGeneratorMenu(int menuId, Container container, MachineBlockEntity.MachineContainerData data, Inventory inventory) {
        super(Hayo.MenuTypes.MATTER_GENERATOR, menuId, container);

        this.addSlot(new Slot(container, 0, 56, 34));
        this.addSlot(new ResultSlot(container, 1, 116, 26));

        this.addStandardInventorySlots(inventory, 8, 110);

        this.machineData = data;
        this.addDataSlots(this.machineData);

        var level = inventory.player.level();
        this.recipes = level.recipeAccess().getSynchronizedRecipes().getAllOfType(Hayo.RecipeTypes.MATTER_GENERATING) //
                .stream() //
                .sorted((holder1, holder2) -> compare(holder1.value(), holder2.value())) //
                .toList();

        this.selectedRecipe = (container instanceof MatterGeneratorBlockEntity entity) ? createSelectedIdx(entity, this.recipes) : DataSlot.standalone();
        this.addDataSlot(this.selectedRecipe);
    }

    public int getSelectedRecipe() {
        return this.selectedRecipe.get();
    }

    public static int compare(MatterGeneratingRecipe recipe1, MatterGeneratingRecipe recipe2) {
        int compareEnergyCost = Integer.compare(recipe1.energyCost(), recipe2.energyCost());
        if (compareEnergyCost != 0) {
            return compareEnergyCost;
        }

        var resultId1 = recipe1.result().item().unwrapKey().orElseThrow().identifier();
        var resultId2 = recipe2.result().item().unwrapKey().orElseThrow().identifier();
        var resultNamespace1 = resultId1.getNamespace();
        var resultNamespace2 = resultId2.getNamespace();

        int compareResultIsVanilla = Boolean.compare(resultNamespace1.equals("minecraft"), resultNamespace2.equals("minecraft"));
        if (compareResultIsVanilla != 0) {
            return compareEnergyCost;
        }

        int compareResultNamespace = resultNamespace1.compareTo(resultNamespace2);
        if (compareResultNamespace != 0) {
            return compareResultNamespace;
        }

        return resultId1.getPath().compareTo(resultId2.getPath());
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
    protected boolean handleQuickMoveFromInventory(ItemStack stack, Player player, int index) {
        if (EnergyComponents.canChargeMachine(stack)) {
            return this.moveItemStackTo(stack, 0, 1, false);
        }
        return false;
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId >= 0 && buttonId < this.recipes.size()) {
            this.selectedRecipe.set(buttonId);

            return true;
        }

        return super.clickMenuButton(player, buttonId);
    }
}
