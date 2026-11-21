package hayo.mod_support.rrv;

import cc.cassian.rrv.api.client.RecipeScreenContext;
import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import hayo.Hayo;
import hayo.common.HayoGuiSprites;
import hayo.energy.EnergyTexts;
import hayo.energy.client.EnergyGuiSprites;
import hayo.features.fluid_stack.FluidGuiRendering;
import hayo.features.processing_machine.solid_fluid_reactor.SolidFluidReactingRecipe;
import hayo.features.processing_machine.solid_fluid_reactor.SolidFluidReactorBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static hayo.common.HayoGuiSprites.REACTING_ARROW;
import static hayo.common.HayoGuiSprites.REACTING_ARROW_OVERLAY;

public class SolidFluidReactingRrvRecipe implements ReliableClientRecipe {
    public static final ReliableClientRecipeType TYPE = new ReliableClientRecipeType() {
        @Override
        public Component getDisplayName() {
            return Component.translatable("hayo.recipe_type.solid_fluid_reacting");
        }

        @Override
        public int getDisplayWidth() {
            return 140 + 6;
        }

        @Override
        public int getDisplayHeight() {
            return 54 + 8;
        }

        @Override
        public @Nullable Identifier getGuiTexture() {
            return null;
        }

        @Override
        public void placeSlots(RecipeViewMenu.SlotDefinition slotDefinition) {
            slotDefinition.addItemSlot(0, 25, 15);

            for (int i = 0; i < 6; i++) {
                slotDefinition.addItemSlot(1 + i, 88 + (i / 3) * 18, 5 + (i % 3) * 18);
            }
        }

        @Override
        public Identifier getId() {
            return Hayo.modId("solid_fluid_reacting");
        }

        @Override
        public ItemStack getIcon() {
            return new ItemStack(Hayo.Items.SOLID_FLUID_REACTOR);
        }

        @Override
        public List<ItemStack> getCraftReferences() {
            return List.of(new ItemStack(Hayo.Items.SOLID_FLUID_REACTOR));
        }
    };

    private final RecipeHolder<SolidFluidReactingRecipe> holder;

    public SolidFluidReactingRrvRecipe(RecipeHolder<SolidFluidReactingRecipe> holder) {
        this.holder = holder;
    }

    @Override
    public ReliableClientRecipeType getType() {
        return TYPE;
    }

    @Override
    public void bindSlots(RecipeViewMenu.SlotFillContext context) {
        var recipe = this.holder.value();

        if (recipe.inputCount > 1) {
            context.bindSlot(0, SlotContent.of(recipe.inputItem.items().map(item -> new ItemStack(item, recipe.inputCount)).toList()));
        } else {
            context.bindSlot(0, SlotContent.of(recipe.inputItem));
        }

        for (int i = 0; i < recipe.resultItems.size(); i++) {
            var result = recipe.resultItems.get(i);
            if (result.chance() == 1F) {
                context.bindSlot(1 + i, SlotContent.of(result.template()));
            }
        }
    }

    @Override
    public List<SlotContent> getIngredients() {
        return List.of(SlotContent.of(this.holder.value().inputItem));
    }

    @Override
    public List<SlotContent> getResults() {
        return this.holder.value().resultItems.stream().map(entry -> SlotContent.of(entry.template())).toList();
    }

    @Override
    public void renderRecipe(RecipeScreenContext context) {
        var graphics = context.guiGraphics();
        var position = context.recipePosition();
        int mouseX = context.mouseX();
        int mouseY = context.mouseY();

        var recipe = this.holder.value();
        int energyCost = recipe.energyCost;
        int useRate = SolidFluidReactorBlockEntity.REACTING_ENERGY_RATE;
        long duration = Mth.positiveCeilDiv(energyCost, useRate) * 50L;

        HayoGuiSprites.blitRecipeViewBackground(graphics, 0, 0, position.width(), position.height());
        HayoGuiSprites.blitSlot(graphics, 24, 14);
        HayoGuiSprites.blitSlots(graphics, 87, 4, 2, 3);

        for (int i = 0; i < recipe.resultItems.size(); i++) {
            var result = recipe.resultItems.get(i);

            if (result.chance() != 1F) {
                graphics.fakeItem(result.template().create(), 88 + (i / 3) * 18, 5 + (i % 3) * 18);
                // TODO: draw chance, show tooltip
            }
        }

        EnergyGuiSprites.blitAnimatedZap(graphics, 26, 33, 5000);
        HayoGuiSprites.blitAnimatedRecipeArrow(graphics, 51, 16, REACTING_ARROW, REACTING_ARROW_OVERLAY, duration);
        graphics.text(context.font(), EnergyTexts.amount(energyCost), 42, 37, 0xFF404040, false);

        FluidGuiRendering.extractFluidTank(graphics, recipe.inputFluid, SolidFluidReactorBlockEntity.FLUID_CAPACITY, 3, 3);
        if (3 <= mouseX && mouseX < 3 + 18 && 3 <= mouseY && mouseY < 3 + 56) {
            graphics.setComponentTooltipForNextFrame(context.font(), FluidGuiRendering.createTooltip(recipe.inputFluid), position.left() + mouseX, position.top() + mouseY);
        }

        FluidGuiRendering.extractFluidTank(graphics, recipe.resultFluid, SolidFluidReactorBlockEntity.FLUID_CAPACITY, 125, 3);
        if (125 <= mouseX && mouseX < 125 + 18 && 3 <= mouseY && mouseY < 3 + 56) {
            graphics.setComponentTooltipForNextFrame(context.font(), FluidGuiRendering.createTooltip(recipe.resultFluid), position.left() + mouseX, position.top() + mouseY);
        }
    }
}
