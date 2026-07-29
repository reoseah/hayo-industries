package io.github.reoseah.hayo.mod_support.jei;

import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import io.github.reoseah.hayo.feature.energy.EnergyGuiSprites;
import io.github.reoseah.hayo.feature.energy.EnergyTexts;
import io.github.reoseah.hayo.feature.machines.ClassicMachineRecipe;
import io.github.reoseah.hayo.feature.machines.matter_generator.MatterGeneratingRecipe;
import io.github.reoseah.hayo.feature.machines.matter_generator.MatterGeneratorBlockEntity;
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
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

import java.util.List;

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
        return Component.translatable("hayo.recipe_type.matter_generating");
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
    public void draw(RecipeHolder<MatterGeneratingRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        EnergyGuiSprites.energySmall(graphics, 3, 6, 10, 14);

        int energyCost = recipe.value().energyCost();
        int progress = (int) ((System.currentTimeMillis() / (TICK_IN_MILLISECONDS * energyCost / ENERGY_USE_RATE / 24)) % 24d);
        HayoGuiSprites.drawRecipeArrow(graphics, 22, 4, HayoGuiSprites.RecipeArrow.DEFAULT, progress, 24);

        graphics.text(Minecraft.getInstance().font, EnergyTexts.amount(energyCost), 1, 28, 0xFF404040, false);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<MatterGeneratingRecipe> holder, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (mouseX >= 3 && mouseX <= 3 + 14 && mouseY >= 6 && mouseY <= 6 + 14 //
                || mouseX > 22 && mouseX <= 22 + 24 && mouseY > 4 && mouseY <= 4 + 16) {
            var recipe = holder.value();
            float duration = Mth.positiveCeilDiv(recipe.energyCost(), MatterGeneratorBlockEntity.ENERGY_USE_RATE) / 20F;

            tooltip.addAll(List.of( //
                    EnergyTexts.amount(recipe.energyCost()), //
                    EnergyTexts.durationAtAmountPerTick(duration, MatterGeneratorBlockEntity.ENERGY_USE_RATE).withStyle(ChatFormatting.GRAY)));
        }
    }
}
