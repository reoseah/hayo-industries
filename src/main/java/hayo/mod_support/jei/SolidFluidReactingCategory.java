package hayo.mod_support.jei;

import hayo.common.HayoGuiSprites;
import hayo.energy.EnergyTexts;
import hayo.features.fluid_stack.FluidGuiRendering;
import hayo.features.processing_machine.solid_fluid_reactor.BaseSolidFluidReactingRecipe;
import hayo.features.processing_machine.solid_fluid_reactor.SolidFluidReactorBlockEntity;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

import static hayo.mod_support.jei.HayoJeiWidgets.UPGRADE_SLOT_DRAWABLE;

public class SolidFluidReactingCategory extends AbstractRecipeCategory<RecipeHolder<BaseSolidFluidReactingRecipe>> {
    public final ItemStack requiredUpgrade;

    public SolidFluidReactingCategory(IRecipeType<RecipeHolder<BaseSolidFluidReactingRecipe>> type, Component name, IDrawable icon, ItemStack requiredUpgrade) {
        super(type, name, icon, 140 + (requiredUpgrade.isEmpty() ? 0 : 20), 54);
        this.requiredUpgrade = requiredUpgrade;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<BaseSolidFluidReactingRecipe> holder, IFocusGroup focuses) {
        var recipe = holder.value();

        if (!this.requiredUpgrade.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 1, 18)
                    .setBackground(UPGRADE_SLOT_DRAWABLE, -1, -1)
                    .addRichTooltipCallback((_, tooltip) -> tooltip.add(Component.translatable("hayo.required_upgrade").withStyle(ChatFormatting.YELLOW)))
                    .add(this.requiredUpgrade);
        }

        int left = !this.requiredUpgrade.isEmpty() ? 20 : 0;

        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).add(
                recipe.inputFluid.values().stream().findAny().map(Holder::value).orElse(Fluids.EMPTY),
                recipe.inputFluid.amount()
        );

        var inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, left + 23, 10).setStandardSlotBackground();
        if (recipe.inputCount != 1) {
            inputSlot.addItemStacks(recipe.inputItem.items().map(item -> new ItemStack(item, recipe.inputCount)).toList());
        } else {
            inputSlot.add(recipe.inputItem);
        }

        boolean hasExtraColumn = recipe.resultItems.size() > 3;
        int resultsLeft = left + 84 + (hasExtraColumn ? 0 : 9);

        for (int i = 0; i < recipe.resultItems.size(); i++) {
            var result = recipe.resultItems.get(i);
            if (result.chance() == 1F) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, resultsLeft + 1 + (i / 3) * 18, 1 + (i % 3) * 18)
                        .setStandardSlotBackground()
                        .add(result.template().create());
            }
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<BaseSolidFluidReactingRecipe> holder, IFocusGroup focuses) {
        var recipe = holder.value();

        int energyCost = recipe.energyCost;
        int useRate = SolidFluidReactorBlockEntity.REACTING_ENERGY_RATE;
        float duration = Mth.positiveCeilDiv(energyCost, useRate) / 20F;

        int left = !this.requiredUpgrade.isEmpty() ? 20 : 0;
        boolean hasExtraColumn = recipe.resultItems.size() > 3;
        int resultsLeft = left + 84 + (hasExtraColumn ? 0 : 9);

        builder.addDrawableWidget(HayoJeiWidgets.zapWidget(energyCost, useRate))
                .setPosition(left + 24, 29)
                .setTooltip(List.of(
                        EnergyTexts.amount(energyCost),
                        Component.translatable("hayo.machine.recipe_duration", duration, useRate).withStyle(ChatFormatting.GRAY)
                ));
        builder.addText(EnergyTexts.amount(energyCost), Integer.MAX_VALUE, Integer.MAX_VALUE).setColor(0xFF404040).setPosition(left + 40, 33);

        builder.addDrawableWidget(HayoJeiWidgets.fluidTank(recipe.inputFluid, SolidFluidReactorBlockEntity.FLUID_CAPACITY))
                .setPosition(left, -1)
                .setTooltip(tooltip -> tooltip.addAll(FluidGuiRendering.createTooltip(recipe.inputFluid)));

        builder.addDrawableWidget(HayoJeiWidgets.fluidTank(recipe.resultFluid, SolidFluidReactorBlockEntity.FLUID_CAPACITY))
                .setPosition(resultsLeft + 20 + (hasExtraColumn ? 18 : 0), -1)
                .setTooltip(tooltip -> tooltip.addAll(FluidGuiRendering.createTooltip(recipe.resultFluid)));

        builder.addDrawableWidget(HayoJeiWidgets.recipeArrow(energyCost, useRate, HayoGuiSprites.REACTING_ARROW, HayoGuiSprites.REACTING_ARROW_OVERLAY))
                .setPosition(left + 49 + (hasExtraColumn ? 0 : 5), 12);

        for (int i = 0; i < (hasExtraColumn ? 6 : 3); i++) {
            int x = resultsLeft + (i / 3) * 18;
            int y = (i % 3) * 18;

            if (i < recipe.resultItems.size()) {
                var result = recipe.resultItems.get(i);
                if (result.chance() < 1F) {
                    builder.addDrawableWidget(HayoJeiWidgets.itemWithChance(result))
                            .setPosition(x, y)
                            .setTooltip(Component.translatable("hayo.extra_chance_tooltip", String.format("%.0f", 100 * result.chance())).withStyle(ChatFormatting.YELLOW));
                }
            } else {
                builder.addDrawableWidget(HayoJeiWidgets.SLOT_DRAWABLE)
                        .setPosition(x, y);
            }
        }
    }
}
