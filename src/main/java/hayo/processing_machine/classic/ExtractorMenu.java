package hayo.processing_machine.classic;

import hayo.Hayo;
import hayo.processing_machine.MachineRecipeData;
import hayo.processing_machine.MachineUpgradeData;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;

public class ExtractorMenu extends ClassicMachineMenu {
    public ExtractorMenu(int containerId, Inventory inventory) {
        super(Hayo.MenuTypes.EXTRACTOR,
                containerId,
                new SimpleContainer(ClassicMachineBlockEntity.SLOTS),
                new MachineRecipeData.Clientside(),
                new MachineUpgradeData.Clientside(),
                inventory);
    }

    public ExtractorMenu(int containerId, ExtractorBlockEntity entity, Inventory inventory) {
        super(Hayo.MenuTypes.EXTRACTOR,
                containerId,
                entity,
                new MachineRecipeData.Serverside(entity),
                new MachineUpgradeData.Serverside(entity),
                inventory);
    }

    @Override
    protected TagKey<Item> getUpgradeTag() {
        return Hayo.ItemTags.EXTRACTOR_UPGRADES;
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getRecipeType() {
        return Hayo.RecipeTypes.EXTRACTING;
    }
}
