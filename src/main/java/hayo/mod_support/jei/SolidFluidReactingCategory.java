package hayo.mod_support.jei;

import hayo.common.HayoGuiSprites;
import hayo.energy.EnergyTexts;
import hayo.energy.client.EnergyGuiSprites;
import hayo.fluid_stack.FluidGuiRendering;
import hayo.solid_fluid_reactor.SolidFluidReactingRecipe;
import hayo.solid_fluid_reactor.SolidFluidReactorBlockEntity;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluids;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

public class SolidFluidReactingCategory extends AbstractRecipeCategory<RecipeHolder<SolidFluidReactingRecipe>> {
    public SolidFluidReactingCategory(IDrawable icon) {
        super(HayoJeiPlugin.SOLID_FLUID_REACTING, Component.translatable("hayo.recipe_type.solid_fluid_reacting"), icon, 122, 54);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<SolidFluidReactingRecipe> holder, IFocusGroup focuses) {
        var recipe = holder.value();

        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).add(recipe.inputFluid().values().stream().findAny().map(Holder::value).orElse(Fluids.EMPTY), recipe.inputFluid().amount());
        builder.addSlot(RecipeIngredientRole.INPUT, 28-5, 10).add(recipe.inputItem()).setStandardSlotBackground();
        builder.addSlot(RecipeIngredientRole.OUTPUT, 84, 1).add(getOptional(recipe.resultItems(), 0).map(ItemStackTemplate::create).orElse(ItemStack.EMPTY)).setStandardSlotBackground();
        builder.addSlot(RecipeIngredientRole.OUTPUT, 84, 19).add(getOptional(recipe.resultItems(), 1).map(ItemStackTemplate::create).orElse(ItemStack.EMPTY)).setStandardSlotBackground();
        builder.addSlot(RecipeIngredientRole.OUTPUT, 84, 37).add(getOptional(recipe.resultItems(), 2).map(ItemStackTemplate::create).orElse(ItemStack.EMPTY)).setStandardSlotBackground();
    }

    public static <T> Optional<T> getOptional(List<T> list, int index) {
        if (list == null || index < 0 || index >= list.size()) {
            return Optional.empty();
        }
        return Optional.ofNullable(list.get(index));
    }

    @Override
    public void draw(RecipeHolder<SolidFluidReactingRecipe> holder, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        var recipe = holder.value();

        var topLeft = graphics.pose().transform(new Vector3f(0, 0, 1));
        graphics.pose().pushMatrix();
        graphics.pose().translate(-topLeft.x, -topLeft.y);
        FluidGuiRendering.extractFluidTank(
                graphics,
                recipe.inputFluid(),
                SolidFluidReactorBlockEntity.FLUID_CAPACITY,
                (int) (topLeft.x),
                (int) topLeft.y - 1,
                (int) (topLeft.x + mouseX),
                (int) (topLeft.y + mouseY));

        FluidGuiRendering.extractFluidTank(
                graphics,
                recipe.resultFluid(),
                SolidFluidReactorBlockEntity.FLUID_CAPACITY,
                (int) (topLeft.x + 104),
                (int) topLeft.y - 1,
                (int) (topLeft.x + mouseX),
                (int) (topLeft.y + mouseY));

        graphics.pose().popMatrix();

        EnergyGuiSprites.blitZap(graphics, 24, 29, 10, 14);

        int energyCost = recipe.energyCost();
        int energyUseRate = SolidFluidReactorBlockEntity.REACTING_ENERGY_RATE;
        int fill = (int) Math.abs((System.currentTimeMillis() / 50 * energyUseRate) % energyCost);
        HayoGuiSprites.blitRecipeArrow(graphics, 49, 12, HayoGuiSprites.REACTING_ARROW, HayoGuiSprites.REACTING_ARROW_OVERLAY, fill, energyCost);

        var font = Minecraft.getInstance().font;
        graphics.text(font, EnergyTexts.amount(recipe.energyCost()), 40, 33, 0xFF404040, false);
    }
}
