package io.github.reoseah.hayo.mod_support.jei;

import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import io.github.reoseah.hayo.feature.energy.EnergyTexts;
import io.github.reoseah.hayo.feature.processing_machines.electric_furnace.ElectricFurnaceBlockEntity;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ElectricCookingJeiCategory implements IRecipeCategory<RecipeHolder<? extends AbstractCookingRecipe>> {
    private static final int ENERGY_USE_RATE = ElectricFurnaceBlockEntity.ENERGY_USE_RATE;
    private static final int TICK_IN_MILLISECONDS = 50;

    private final IRecipeType<RecipeHolder<? extends AbstractCookingRecipe>> type;
    private final Component title;
    private final IDrawable icon;

    public ElectricCookingJeiCategory(IRecipeType<? extends RecipeHolder<? extends AbstractCookingRecipe>> type, Component title, IDrawable icon) {
        this.type = (IRecipeType<RecipeHolder<? extends AbstractCookingRecipe>>) type;
        this.title = title;
        this.icon = icon;
    }

    @Override
    public IRecipeType<RecipeHolder<? extends AbstractCookingRecipe>> getRecipeType() {
        return this.type;
    }

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public int getWidth() {
        return 88;
    }

    @Override
    public int getHeight() {
        return 45;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<? extends AbstractCookingRecipe> recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 5) //
                .setStandardSlotBackground() //
                .add(recipe.value().input());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 65, 9) //
                .setOutputSlotBackground() //
                .add(recipe.value().result());
    }

    @Override
    public void draw(RecipeHolder<? extends AbstractCookingRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        HayoGuiSprites.drawMachineEnergy(guiGraphics, 5, 24, 10, 14);

        int energyCost = ElectricFurnaceBlockEntity.energyCostFromCookingTime(recipe.value().cookingTime());
        int progress = (int) ((System.currentTimeMillis() / (TICK_IN_MILLISECONDS * energyCost / ENERGY_USE_RATE / 24)) % 24d);
        HayoGuiSprites.drawRecipeArrow(guiGraphics, 28, 8, HayoGuiSprites.RecipeArrow.DEFAULT, progress, 24);

        guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, EnergyTexts.amount(energyCost), 23, 28, 0xFF404040, false);
    }
}
