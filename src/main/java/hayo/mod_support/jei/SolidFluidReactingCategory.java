package hayo.mod_support.jei;

import hayo.common.HayoGuiSprites;
import hayo.energy.EnergyTexts;
import hayo.features.fluid_stack.FluidGuiRendering;
import hayo.features.processing_machine.solid_fluid_reactor.SolidFluidReactingRecipe;
import hayo.features.processing_machine.solid_fluid_reactor.SolidFluidReactorBlockEntity;
import hayo.mod_support.jei.widgets.AnimatedSpriteJeiDrawable;
import hayo.mod_support.jei.widgets.FluidTankJeiDrawable;
import hayo.mod_support.jei.widgets.HayoJeiWidgets;
import hayo.mod_support.jei.widgets.SpriteJeiDrawable;
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

public class SolidFluidReactingCategory extends AbstractRecipeCategory<RecipeHolder<SolidFluidReactingRecipe>> {
    public SolidFluidReactingCategory(IRecipeType<RecipeHolder<SolidFluidReactingRecipe>> type, Component name, IDrawable icon, ItemStack requiredUpgrade) {
        super(type, name, icon, 140, 54);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<SolidFluidReactingRecipe> holder, IFocusGroup focuses) {
        var recipe = holder.value();

        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).add(
                recipe.inputFluid.values().stream().findAny().map(Holder::value).orElse(Fluids.EMPTY),
                recipe.inputFluid.amount() * 81L
        );

        var inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 23, 10).setStandardSlotBackground();
        if (recipe.inputCount != 1) {
            inputSlot.addItemStacks(recipe.inputItem.items().map(item -> new ItemStack(item, recipe.inputCount)).toList());
        } else {
            inputSlot.add(recipe.inputItem);
        }

        boolean hasExtraColumn = recipe.resultItems.size() > 3;
        int resultsLeft = 84 + (hasExtraColumn ? 0 : 9);

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
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<SolidFluidReactingRecipe> holder, IFocusGroup focuses) {
        var recipe = holder.value();

        int energyCost = recipe.energyCost;
        int useRate = SolidFluidReactorBlockEntity.REACTING_ENERGY_RATE;
        float duration = Mth.positiveCeilDiv(energyCost, useRate) / 20F;

        boolean hasSecondColumn = recipe.resultItems.size() > 3;

        builder.addDrawableWidget(AnimatedSpriteJeiDrawable.zapWidget(energyCost, useRate))
                .setPosition(24, 29)
                .setTooltip(List.of(
                        EnergyTexts.amount(energyCost),
                        Component.translatable("hayo.machine.recipe_duration", duration, useRate).withStyle(ChatFormatting.GRAY)
                ));
        builder.addText(EnergyTexts.amount(energyCost), Integer.MAX_VALUE, Integer.MAX_VALUE)
                .setColor(0xFF404040)
                .setPosition(40, 33);

        builder.addDrawableWidget(FluidTankJeiDrawable.ofIngredient(recipe.inputFluid, SolidFluidReactorBlockEntity.FLUID_CAPACITY))
                .setPosition(0, -1)
                .setTooltip(tooltip -> tooltip.addAll(FluidGuiRendering.createTooltip(recipe.inputFluid)));

        builder.addDrawableWidget(FluidTankJeiDrawable.ofStack(recipe.resultFluid, SolidFluidReactorBlockEntity.FLUID_CAPACITY))
                .setPosition(113 + (hasSecondColumn ? 9 : 0), -1)
                .setTooltip(tooltip -> tooltip.addAll(FluidGuiRendering.createTooltip(recipe.resultFluid)));

        builder.addDrawableWidget(AnimatedSpriteJeiDrawable.recipeArrow(energyCost, useRate, HayoGuiSprites.REACTING_ARROW, HayoGuiSprites.REACTING_ARROW_OVERLAY))
                .setPosition(49 + (hasSecondColumn ? 0 : 5), 12);

        for (int i = 0; i < (hasSecondColumn ? 6 : 3); i++) {
            int x = 84 + (hasSecondColumn ? 0 : 9) + (i / 3) * 18;
            int y = (i % 3) * 18;

            if (i < recipe.resultItems.size()) {
                var result = recipe.resultItems.get(i);
                if (result.chance() < 1F) {
                    builder.addDrawableWidget(HayoJeiWidgets.itemWithChance(result))
                            .setPosition(x, y)
                            .setTooltip(Component.translatable("hayo.extra_chance_tooltip", String.format("%.0f", 100 * result.chance())).withStyle(ChatFormatting.YELLOW));
                }
            } else {
                builder.addDrawableWidget(SpriteJeiDrawable.SLOT)
                        .setPosition(x, y);
            }
        }
    }
}
