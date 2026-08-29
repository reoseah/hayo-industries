package hayo.processing_machine.classic;

import hayo.Hayo;
import hayo.processing_machine.MachineContainerData;
import hayo.processing_machine.UpgradableMachineContainerData;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;

public class ElectricFurnaceMenu extends ClassicMachineMenu {
    private final DataSlot modeData;

    public ElectricFurnaceMenu(int containerId, Inventory inventory) {
        super(Hayo.MenuTypes.ELECTRIC_FURNACE,
                containerId,
                new SimpleContainer(ClassicMachineBlockEntity.SLOTS),
                new MachineContainerData.Clientside(),
                new UpgradableMachineContainerData.Clientside(),
                inventory);
        this.modeData = DataSlot.standalone();
        this.addDataSlot(this.modeData);
    }

    public ElectricFurnaceMenu(int containerId, ElectricFurnaceBlockEntity entity, Inventory inventory) {
        super(Hayo.MenuTypes.ELECTRIC_FURNACE,
                containerId,
                entity,
                new MachineContainerData.Serverside(entity),
                new UpgradableMachineContainerData.Serverside(entity),
                inventory);
        this.modeData = new DataSlot() {
            @Override
            public int get() {
                return entity.getMode().ordinal();
            }

            @Override
            public void set(int value) {

            }
        };
        this.addDataSlot(this.modeData);
    }

    public ElectricFurnaceMode getModeData() {
        return ElectricFurnaceMode.values()[Math.clamp(this.modeData.get(), 0, ElectricFurnaceMode.values().length - 1)];
    }

    @Override
    protected TagKey<Item> getUpgradeTag() {
        return Hayo.ItemTags.ELECTRIC_FURNACE_UPGRADES;
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getRecipeType() {
        return this.getModeData().recipeType;
    }
}
