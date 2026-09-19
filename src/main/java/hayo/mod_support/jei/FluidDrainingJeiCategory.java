package hayo.mod_support.jei;

import hayo.common.HayoGuiSprites;
import hayo.energy.EnergyTexts;
import hayo.energy.client.EnergyGuiSprites;
import hayo.fluid_stack.FluidDrainingRecipe;
import hayo.fluid_stack.FluidGuiRendering;
import hayo.processing_machine.classic.ElectricFurnaceBlockEntity;
import hayo.solid_fluid_reactor.SolidFluidReactorBlockEntity;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.joml.Vector3f;
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
    public void draw(RecipeHolder<FluidDrainingRecipe> holder, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        int energyCost = holder.value().energyCost();
        float fill = (System.currentTimeMillis() / 50 * ElectricFurnaceBlockEntity.ENERGY_USE_RATE) % energyCost;
        HayoGuiSprites.blitDrainingArrow(graphics, 6, 20, (int) fill, energyCost);

        var topLeft = graphics.pose().transform(new Vector3f(0, 0, 1));
        graphics.pose().pushMatrix();
        graphics.pose().translate(-topLeft.x, -topLeft.y);
        FluidGuiRendering.extractFluidTank(
                graphics,
                holder.value().resultFluid(),
                SolidFluidReactorBlockEntity.FLUID_CAPACITY,
                (int) (topLeft.x + 18 + 9),
                (int) topLeft.y - 1,
                (int) (topLeft.x + mouseX),
                (int) (topLeft.y + mouseY));
        graphics.pose().popMatrix();

        EnergyGuiSprites.blitZap(graphics, 51, 19, 10, 14);

        graphics.text(Minecraft.getInstance().font, EnergyTexts.amount(energyCost), 50, 42, 0xFF404040, false);
    }
}
