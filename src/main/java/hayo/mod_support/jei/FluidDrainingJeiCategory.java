package hayo.mod_support.jei;

import hayo.common.HayoGuiSprites;
import hayo.energy.EnergyTexts;
import hayo.fluid_stack.FluidDrainingRecipe;
import hayo.fluid_stack.FluidGuiRendering;
import hayo.processing_machine.solid_fluid_reactor.SolidFluidReactorBlockEntity;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class FluidDrainingJeiCategory implements IRecipeCategory<RecipeHolder<FluidDrainingRecipe>> {
    private final IDrawable icon;

    public FluidDrainingJeiCategory(IDrawable icon) {
        this.icon = icon;
    }

    @Override
    public IRecipeType<RecipeHolder<FluidDrainingRecipe>> getRecipeType() {
        return HayoJeiPlugin.FLUID_DRAINING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("hayo.recipe_type.fluid_draining");
    }

    @Override
    public int getWidth() {
        return 90;
    }

    @Override
    public int getHeight() {
        return 54;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<FluidDrainingRecipe> holder, IFocusGroup focuses) {
        var recipe = holder.value();

        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).add(recipe.input()).setStandardSlotBackground();
        builder.addSlot(RecipeIngredientRole.OUTPUT, 1, 37).add(recipe.resultItem()).setStandardSlotBackground();
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).add(recipe.resultFluid().fluid(), recipe.resultFluid().amount());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<FluidDrainingRecipe> holder, IFocusGroup focuses) {
        var recipe = holder.value();

        int energyCost = recipe.energyCost();
        int useRate = 1;
        float duration = Mth.positiveCeilDiv(energyCost, useRate) / 20F;

        builder.addDrawableWidget(HayoJeiWidgets.zapWidget(recipe.energyCost(), 1)).setPosition(51, 19).setTooltip(List.of(
                EnergyTexts.amount(energyCost),
                Component.translatable("hayo.machine.recipe_duration", duration, useRate).withStyle(ChatFormatting.GRAY)
        ));
        builder.addText(EnergyTexts.amount(recipe.energyCost()), Integer.MAX_VALUE, Integer.MAX_VALUE).setColor(0xFF404040).setPosition(50, 42);

        builder.addDrawableWidget(HayoJeiWidgets.fluidTank(recipe.resultFluid(), SolidFluidReactorBlockEntity.FLUID_CAPACITY))
                .setPosition(27, -1)
                .setTooltip(tooltip -> tooltip.addAll(FluidGuiRendering.createTooltip(recipe.resultFluid())));
    }

    @Override
    public void draw(RecipeHolder<FluidDrainingRecipe> holder, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        int energyCost = holder.value().energyCost();
        float fill = (System.currentTimeMillis() / 50) % energyCost;
        HayoGuiSprites.blitDrainingArrow(graphics, 6, 20, (int) fill, energyCost);
    }

}
