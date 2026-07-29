package io.github.reoseah.hayo.mod_support.jei;

import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import io.github.reoseah.hayo.feature.energy.EnergyGuiSprites;
import io.github.reoseah.hayo.feature.energy.EnergyTexts;
import io.github.reoseah.hayo.feature.machines.electric_furnace.ElectricFurnaceBlockEntity;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

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
        return 82;
    }

    @Override
    public int getHeight() {
        return 35;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<? extends AbstractCookingRecipe> recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1) //
                .setStandardSlotBackground() //
                .add(recipe.value().input());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 5) //
                .setOutputSlotBackground() //
                .add(recipe.value().result().create());
    }

    @Override
    public void draw(RecipeHolder<? extends AbstractCookingRecipe> holder, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        EnergyGuiSprites.energySmall(graphics, 1, 20, 10, 14);

        int energyCost = ElectricFurnaceBlockEntity.energyCostFromCookingTime(holder.value());
        int progress = (int) ((System.currentTimeMillis() / (TICK_IN_MILLISECONDS * energyCost / ENERGY_USE_RATE / 24)) % 24d);
        HayoGuiSprites.drawRecipeArrow(graphics, 24, 4, HayoGuiSprites.RecipeArrow.DEFAULT, progress, 24);

        graphics.text(Minecraft.getInstance().font, EnergyTexts.amount(energyCost), 19, 24, 0xFF404040, false);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<? extends AbstractCookingRecipe> holder, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (mouseX >= 1 && mouseX <= 1 + 24 && mouseY >= 20 && mouseY <= 20 + 24 //
                || mouseX > 24 && mouseX <= 24 + 24 && mouseY > 4 && mouseY <= 4 + 16) {
            var recipe = holder.value();

            int energyCost = ElectricFurnaceBlockEntity.energyCostFromCookingTime(recipe);
            int progress = (int) ((System.currentTimeMillis() / (TICK_IN_MILLISECONDS * energyCost / ENERGY_USE_RATE / 24)) % 24d);
            float duration = Mth.positiveCeilDiv(energyCost, ENERGY_USE_RATE) / 20F;

            tooltip.addAll(List.of( //
                    EnergyTexts.amount(energyCost), //
                    EnergyTexts.durationAtAmountPerTick(duration, ENERGY_USE_RATE).withStyle(ChatFormatting.GRAY)));
        }
    }
}
