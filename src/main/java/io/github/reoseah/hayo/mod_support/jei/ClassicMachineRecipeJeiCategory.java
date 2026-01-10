package io.github.reoseah.hayo.mod_support.jei;

import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import io.github.reoseah.hayo.feature.energy.EnergyTexts;
import io.github.reoseah.hayo.feature.processing_machines.ClassicMachineRecipe;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;

public class ClassicMachineRecipeJeiCategory implements IRecipeCategory<ClassicMachineRecipe> {
    private static final int TICK_IN_MILLISECONDS = 50;

    private final IRecipeType<? extends ClassicMachineRecipe> type;
    private final int energyUseRate;
    private final HayoGuiSprites.RecipeArrow arrowType;
    private final Component title;
    private final IDrawable icon;

    public ClassicMachineRecipeJeiCategory(IRecipeType<? extends ClassicMachineRecipe> type, int energyUseRate, HayoGuiSprites.RecipeArrow arrowType, Component title, IDrawable icon) {
        this.type = type;
        this.energyUseRate = energyUseRate;
        this.arrowType = arrowType;
        this.title = title;
        this.icon = icon;
    }

    @Override
    public IRecipeType<ClassicMachineRecipe> getRecipeType() {
        return (IRecipeType<ClassicMachineRecipe>) this.type;
    }

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public int getWidth() {
        return 110;
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
    public void setRecipe(IRecipeLayoutBuilder builder, ClassicMachineRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 5) //
                .setStandardSlotBackground() //
                .add(recipe.input());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 65, 9) //
                .setOutputSlotBackground() //
                .add(recipe.result());
        if (recipe.extraChance() > 0) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 89, 5).setStandardSlotBackground().add(recipe.result().copyWithCount(1));
        }
    }

    @Override
    public void draw(ClassicMachineRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        HayoGuiSprites.drawMachineEnergy(graphics, 5, 24, 10, 14);

        int energyCost = recipe.getEnergyCost();
        int progress = (int) ((System.currentTimeMillis() / (TICK_IN_MILLISECONDS * energyCost / this.energyUseRate / 24)) % 24d);
        HayoGuiSprites.drawRecipeArrow(graphics, 28, 8, HayoGuiSprites.RecipeArrow.DEFAULT, progress, 24);

        graphics.drawString(net.minecraft.client.Minecraft.getInstance().font, EnergyTexts.amount(energyCost), 23, 28, 0xFF404040, false);

        if (recipe.extraChance() > 0) {
            Component chance = Component.translatable("hayo.chance.percentage", String.format("%.0f", 100 * recipe.extraChance()));
            graphics.drawString(Minecraft.getInstance().font, chance, 89, 28, 0xFF404040, false);
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, ClassicMachineRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (mouseX >= 5 && mouseX <= 5 + 14 && mouseY >= 24 && mouseY <= 24 + 14) {
            tooltip.addAll(List.of(EnergyTexts.amount(recipe.getEnergyCost()), //
                    EnergyTexts.durationAtAmountPerTick(Mth.positiveCeilDiv(recipe.getEnergyCost(), energyUseRate) / 20F, energyUseRate).withStyle(ChatFormatting.GRAY)));
        }
    }
}
