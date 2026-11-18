package hayo.mod_support.rrv;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import hayo.common.HayoGuiSprites;
import hayo.features.processing_machine.matter_generator.MatterGeneratingRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class MatterGeneratingRrvRecipe implements ReliableClientRecipe {
    private final Identifier id;
    private final SlotContent upgradeItem, resultItem;

    public MatterGeneratingRrvRecipe(RecipeHolder<MatterGeneratingRecipe> holder) {
        this(
                holder.id().identifier(),
                holder.value().requiredUpgrade().value() != Items.AIR ? SlotContent.of(holder.value().requiredUpgrade().value()) : SlotContent.of(),
                SlotContent.of(holder.value().result())
        );
    }

    public MatterGeneratingRrvRecipe(Identifier id, SlotContent upgradeItem, SlotContent resultItem) {
        this.id = id;
        this.upgradeItem = upgradeItem;
        this.resultItem = resultItem;
    }

    @Override
    public ReliableClientRecipeType getType() {
        return MatterGeneratingRrvType.INSTANCE;
    }

    @Override
    public void bindSlots(RecipeViewMenu.SlotFillContext slotFillContext) {
        if (!this.upgradeItem.isEmpty()) {
            slotFillContext.bindOptionalSlot(0, this.upgradeItem, (guiGraphics, mouseX, mouseY, partialTicks) -> {
                HayoGuiSprites.blitUpgradeSlots(guiGraphics, 0, 0, 1);
            });
        }
        slotFillContext.bindSlot(1, this.resultItem);
    }

    @Override
    public List<SlotContent> getIngredients() {
        return List.of(this.upgradeItem);
    }

    @Override
    public List<SlotContent> getResults() {
        return List.of(this.resultItem);
    }

    @Override
    public Identifier getId() {
        return this.id;
    }
}
