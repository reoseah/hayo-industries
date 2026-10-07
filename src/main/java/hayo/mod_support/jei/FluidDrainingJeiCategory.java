package hayo.mod_support.jei;

import hayo.common.HayoGuiSprites;
import hayo.features.fluid_stack.FluidDrainingRecipe;
import hayo.features.fluid_stack.FluidGuiRendering;
import hayo.features.processing_machine.solid_fluid_reactor.SolidFluidReactorBlockEntity;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

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
        return 100;
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

        builder.addText(Component.translatable("hayo.ticks", recipe.ticks()), Integer.MAX_VALUE, Integer.MAX_VALUE).setColor(0xFF404040).setPosition(50, 42);

        builder.addDrawableWidget(HayoJeiWidgets.fluidTank(recipe.resultFluid(), SolidFluidReactorBlockEntity.FLUID_CAPACITY))
                .setPosition(27, -1)
                .setTooltip(tooltip -> tooltip.addAll(FluidGuiRendering.createTooltip(recipe.resultFluid())));
    }

    @Override
    public void draw(RecipeHolder<FluidDrainingRecipe> holder, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        int energyCost = holder.value().ticks();
        float fill = (System.currentTimeMillis() / 50) % energyCost;
        HayoGuiSprites.blitDrainingArrow(graphics, 6, 20, (int) fill, energyCost);
    }
}
