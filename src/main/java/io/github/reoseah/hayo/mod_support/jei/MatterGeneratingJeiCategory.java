package io.github.reoseah.hayo.mod_support.jei;

import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import io.github.reoseah.hayo.feature.energy.EnergyTexts;
import io.github.reoseah.hayo.feature.processing_machines.matter_generator.MatterGeneratingRecipe;
import io.github.reoseah.hayo.feature.processing_machines.matter_generator.MatterGeneratorBlockEntity;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

public class MatterGeneratingJeiCategory implements IRecipeCategory<RecipeHolder<MatterGeneratingRecipe>> {
    private static final int ENERGY_USE_RATE = MatterGeneratorBlockEntity.ENERGY_USE_RATE;
    private static final int TICK_IN_MILLISECONDS = 50;

    private final IDrawable icon;

    public MatterGeneratingJeiCategory(IDrawable icon) {
        this.icon = icon;
    }

    @Override
    public IRecipeType<RecipeHolder<MatterGeneratingRecipe>> getRecipeType() {
        return HayoJeiPlugin.MATTER_GENERATING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("hayo.matter_generating");
    }

    @Override
    public int getWidth() {
        return 80;
    }

    @Override
    public int getHeight() {
        return 36;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<MatterGeneratingRecipe> recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.OUTPUT, 59, 5) //
                .setOutputSlotBackground() //
                .add(recipe.value().result().create());
    }

    @Override
    public void draw(RecipeHolder<MatterGeneratingRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        HayoGuiSprites.drawMachineEnergy(graphics, 3, 24 - 18, 10, 14);

        int energyCost = recipe.value().getEnergyCost();
        int progress = (int) ((System.currentTimeMillis() / (TICK_IN_MILLISECONDS * energyCost / ENERGY_USE_RATE / 24)) % 24d);
        HayoGuiSprites.drawRecipeArrow(graphics, 22, 4, HayoGuiSprites.RecipeArrow.DEFAULT, progress, 24);

        graphics.drawString(net.minecraft.client.Minecraft.getInstance().font, EnergyTexts.amount(energyCost), 1, 28, 0xFF404040, false);
    }
}
