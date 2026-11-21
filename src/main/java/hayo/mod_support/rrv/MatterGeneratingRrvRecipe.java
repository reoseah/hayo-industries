package hayo.mod_support.rrv;

import cc.cassian.rrv.api.client.RecipeScreenContext;
import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeSlot;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewScreen;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import hayo.Hayo;
import hayo.common.HayoGuiSprites;
import hayo.energy.EnergyTexts;
import hayo.energy.client.EnergyGuiSprites;
import hayo.features.processing_machine.matter_generator.MatterGeneratingRecipe;
import hayo.features.processing_machine.matter_generator.MatterGeneratorBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static hayo.common.HayoGuiSprites.DEFAULT_ARROW;
import static hayo.common.HayoGuiSprites.DEFAULT_ARROW_OVERLAY;

public class MatterGeneratingRrvRecipe implements ReliableClientRecipe {
    public static final ReliableClientRecipeType TYPE = new ReliableClientRecipeType() {
        @Override
        public Component getDisplayName() {
            return Component.translatable("hayo.recipe_type.matter_generating");
        }

        @Override
        public int getDisplayWidth() {
            return 100 + 6;
        }

        @Override
        public int getDisplayHeight() {
            return 36 + 6;
        }

        @Override
        public @Nullable Identifier getGuiTexture() {
            return null;
        }

        @Override
        public void placeSlots(RecipeViewMenu.SlotDefinition slotDefinition) {
            slotDefinition.setHighlightWithoutContents(false);
            slotDefinition.addItemSlot(0, 5, 8);
            slotDefinition.setHighlightWithoutContents(true);
            slotDefinition.addItemSlot(1, 82, 8);
        }

        @Override
        public Identifier getId() {
            return Hayo.modId("matter_generating");
        }

        @Override
        public ItemStack getIcon() {
            return new ItemStack(Hayo.Items.MATTER_GENERATOR);
        }

        @Override
        public List<ItemStack> getCraftReferences() {
            return List.of(new ItemStack(Hayo.Items.MATTER_GENERATOR));
        }
    };

    private final RecipeHolder<MatterGeneratingRecipe> holder;

    public MatterGeneratingRrvRecipe(RecipeHolder<MatterGeneratingRecipe> holder) {
        this.holder = holder;
    }

    @Override
    public ReliableClientRecipeType getType() {
        return TYPE;
    }

    @Override
    public void bindSlots(RecipeViewMenu.SlotFillContext context) {
        var upgrade = this.holder.value().requiredUpgrade().value();
        if (upgrade != Items.AIR) {
            context.bindSlot(0, SlotContent.of(upgrade));
        }
        context.bindSlot(1, SlotContent.of(this.holder.value().result()));
    }

    @Override
    public List<SlotContent> getIngredients() {
        var upgrade = this.holder.value().requiredUpgrade().value();
        return upgrade != Items.AIR ? List.of(SlotContent.of(upgrade)) : List.of();
    }

    @Override
    public List<SlotContent> getResults() {
        return List.of(SlotContent.of(this.holder.value().result()));
    }

    @Override
    public Identifier getId() {
        return this.holder.id().identifier();
    }

    @Override
    public void renderRecipe(RecipeScreenContext context) {
        var graphics = context.guiGraphics();
        var position = context.recipePosition();

        boolean hasUpgrade = this.holder.value().requiredUpgrade().value() != Items.AIR;
        if (!(context.screen() instanceof RecipeViewScreen)) {
            // can't edit slots, so always render with an upgrade slot
            hasUpgrade = true;
        }

        if (!hasUpgrade) {
            var screen = (RecipeViewScreen) context.screen();
            var slots = screen.getMenu().slots;

            for (int i = 0; i < slots.size(); i++) {
                if (slots.get(i) instanceof RecipeSlot slot
                        && slot.x == position.left() - screen.getLeftPos() + 82
                        && slot.y == position.top() - screen.getTopPos() + 8) {
                    slots.set(i, new RecipeSlot(slot.container, slot.index, slot.x - 10, slot.y, true));
                }
            }
        }

        HayoGuiSprites.blitRecipeViewBackground(graphics, hasUpgrade ? 0 : 10, 0, hasUpgrade ? position.width() : position.width() - 20, position.height());
        if (hasUpgrade) {
            HayoGuiSprites.blitUpgradeSlots(graphics, 4, 7, 1);
        }
        HayoGuiSprites.blitOutputSlot(graphics, hasUpgrade ? 78 : 68, 4);

        EnergyGuiSprites.blitAnimatedZap(graphics, hasUpgrade ? 26 : 16, 8, 5000);

        int energyCost = this.holder.value().energyCost();
        long duration = 50L * energyCost / MatterGeneratorBlockEntity.ENERGY_USE_RATE;
        HayoGuiSprites.blitAnimatedRecipeArrow(graphics, hasUpgrade ? 47 : 37, 7, DEFAULT_ARROW, DEFAULT_ARROW_OVERLAY, duration);

        graphics.text(context.font(), EnergyTexts.amount(energyCost), hasUpgrade ? 4 : 14, 31, 0xFF404040, false);
    }
}
