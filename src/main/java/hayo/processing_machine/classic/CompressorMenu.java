package hayo.processing_machine.classic;

import hayo.Hayo;
import hayo.processing_machine.MachineContainerData;
import hayo.processing_machine.UpgradableMachineContainerData;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;

public class CompressorMenu extends ClassicMachineMenu {
    public CompressorMenu(int containerId, Inventory inventory) {
        super(Hayo.MenuTypes.COMPRESSOR,
                containerId,
                new SimpleContainer(ClassicMachineBlockEntity.SLOTS),
                new MachineContainerData.Clientside(),
                new UpgradableMachineContainerData.Clientside(),
                inventory);
    }

    public CompressorMenu(int containerId, CompressorBlockEntity entity, Inventory inventory) {
        super(Hayo.MenuTypes.COMPRESSOR,
                containerId,
                entity,
                new MachineContainerData.Serverside(entity),
                new UpgradableMachineContainerData.Serverside(entity),
                inventory);
    }

    @Override
    protected TagKey<Item> getUpgradeTag() {
        return Hayo.ItemTags.COMPRESSOR_UPGRADES;
    }

    @Override
    protected RecipeType<? extends Recipe<SingleRecipeInput>> getRecipeType() {
        return Hayo.RecipeTypes.COMPRESSING;
    }
}
