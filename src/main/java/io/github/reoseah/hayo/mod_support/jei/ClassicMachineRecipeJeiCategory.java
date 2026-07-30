package io.github.reoseah.hayo.mod_support.jei;

import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import io.github.reoseah.hayo.feature.electric_blocks.EnergyGuiSprites;
import io.github.reoseah.hayo.feature.electric_blocks.EnergyTexts;
import io.github.reoseah.hayo.feature.machines.ClassicMachineRecipe;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class ClassicMachineRecipeJeiCategory implements IRecipeCategory<RecipeHolder<? extends ClassicMachineRecipe>> {
    private static final int TICK_IN_MILLISECONDS = 50;

    private final IRecipeType<? extends RecipeHolder<? extends ClassicMachineRecipe>> type;
    private final int energyUseRate;
    private final HayoGuiSprites.RecipeArrow arrowType;
    private final Component title;
    private final IDrawable icon;

    public ClassicMachineRecipeJeiCategory(IRecipeType<? extends RecipeHolder<? extends ClassicMachineRecipe>> type, int energyUseRate, HayoGuiSprites.RecipeArrow arrowType, Component title, IDrawable icon) {
        this.type = type;
        this.energyUseRate = energyUseRate;
        this.arrowType = arrowType;
        this.title = title;
        this.icon = icon;
    }

    @Override
    public IRecipeType<RecipeHolder<? extends ClassicMachineRecipe>> getRecipeType() {
        return (IRecipeType<RecipeHolder<? extends ClassicMachineRecipe>>) this.type;
    }

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public int getWidth() {
        return 102;
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<? extends ClassicMachineRecipe> holder, IFocusGroup focuses) {
        var recipe = holder.value();
        if (recipe.inputCount == 1) {
            builder.addSlot(RecipeIngredientRole.INPUT, 1, 1) //
                    .setStandardSlotBackground() //
                    .add(recipe.input);
        } else {
            builder.addSlot(RecipeIngredientRole.INPUT, 1, 1) //
                    .setStandardSlotBackground() //
                    .addItemStacks(recipe.input.items().map(item -> new ItemStack(item, recipe.inputCount)).toList());
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 5) //
                .setOutputSlotBackground() //
                .add(recipe.result.create());
        if (recipe.extraResultChance > 0) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 85, 1) //
                    .setStandardSlotBackground() //
                    .addRichTooltipCallback((recipeSlotView, tooltip) -> tooltip.add(Component.translatable("hayo.chance.words", recipe.extraResultChance * 100).withStyle(ChatFormatting.YELLOW)))
                    .add(recipe.result.create().copyWithCount(1));
        }
    }

    @Override
    public void draw(RecipeHolder<? extends ClassicMachineRecipe> holder, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        var recipe = holder.value();
        EnergyGuiSprites.energySmall(graphics, 1, 20, 10, 14);

        int energyCost = recipe.energyCost;
        int progress = (int) ((System.currentTimeMillis() / (TICK_IN_MILLISECONDS * energyCost / this.energyUseRate / 24)) % 24d);
        HayoGuiSprites.drawRecipeArrow(graphics, 24, 4, this.arrowType, progress, 24);

        var font = Minecraft.getInstance().font;

        graphics.text(font, EnergyTexts.amount(energyCost), 19, 24, 0xFF404040, false);
        if (recipe.extraResultChance > 0) {
            var extraChance = Component.translatable("hayo.chance.percentage", String.format("%.0f", 100 * recipe.extraResultChance));
            graphics.text(font, extraChance, 85, 24, 0xFF404040, false);
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<? extends ClassicMachineRecipe> holder, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (mouseX >= 1 && mouseX <= 1 + 14 && mouseY >= 20 && mouseY <= 20 + 14 //
                || mouseX > 24 && mouseX <= 24 + 24 && mouseY > 4 && mouseY <= 4 + 16) {
            var recipe = holder.value();
            float duration = Mth.positiveCeilDiv(recipe.energyCost, this.energyUseRate) / 20F;

            tooltip.addAll(List.of( //
                    Component.translatable("hayo.duration.seconds", duration), //
                    Component.translatable("hayo.energy.amount_and_amount_per_tick", recipe.energyCost, this.energyUseRate).withStyle(ChatFormatting.GRAY)));
        }
    }
}
