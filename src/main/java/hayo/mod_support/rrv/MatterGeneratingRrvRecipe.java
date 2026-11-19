package hayo.mod_support.rrv;

import cc.cassian.rrv.api.client.RecipeScreenContext;
import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import hayo.common.HayoGuiSprites;
import hayo.energy.EnergyTexts;
import hayo.energy.client.EnergyGuiSprites;
import hayo.features.processing_machine.matter_generator.MatterGeneratingRecipe;
import hayo.features.processing_machine.matter_generator.MatterGeneratorBlockEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class MatterGeneratingRrvRecipe implements ReliableClientRecipe {
    private final Identifier id;
    private final SlotContent upgradeItem, resultItem;
    private final int energyCost;

    public MatterGeneratingRrvRecipe(RecipeHolder<MatterGeneratingRecipe> holder) {
        this(
                holder.id().identifier(),
                holder.value().requiredUpgrade().value() != Items.AIR ? SlotContent.of(holder.value().requiredUpgrade().value()) : SlotContent.of(),
                SlotContent.of(holder.value().result()),
                holder.value().energyCost()
        );
    }

    public MatterGeneratingRrvRecipe(Identifier id, SlotContent upgradeItem, SlotContent resultItem, int energyCost) {
        this.id = id;
        this.upgradeItem = upgradeItem;
        this.resultItem = resultItem;
        this.energyCost = energyCost;
    }

    @Override
    public ReliableClientRecipeType getType() {
        return MatterGeneratingRrvType.INSTANCE;
    }

    @Override
    public void bindSlots(RecipeViewMenu.SlotFillContext slotFillContext) {
        if (!this.upgradeItem.isEmpty()) {
            slotFillContext.bindSlot(0, this.upgradeItem);
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

    @Override
    public void renderRecipe(RecipeScreenContext context) {
        var graphics = context.guiGraphics();
        var position = context.recipePosition();

        HayoGuiSprites.blitRecipeViewBackground(graphics, -1, 0, position.width(), position.height());

        graphics.pose().pushMatrix();
        graphics.pose().translate(3, 3);
        {
            HayoGuiSprites.blitUpgradeSlots(graphics, 1 - 1, 5 - 1, 1);
            HayoGuiSprites.blitOutputSlot(graphics, 79 - 4, 5 - 4);
            EnergyGuiSprites.blitAnimatedZap(graphics, 23, 5, 5000);
            HayoGuiSprites.blitAnimatedRecipeArrow(graphics, 44, 4, HayoGuiSprites.DEFAULT_ARROW, HayoGuiSprites.DEFAULT_ARROW_OVERLAY,
                    50L * this.energyCost / MatterGeneratorBlockEntity.ENERGY_USE_RATE
            );

            graphics.text(context.font(), EnergyTexts.amount(this.energyCost), 1, 28, 0xFF404040, false);
        }
        graphics.pose().popMatrix();

        ReliableClientRecipe.super.renderRecipe(context);
    }
}
