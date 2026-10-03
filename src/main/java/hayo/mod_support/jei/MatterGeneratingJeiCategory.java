package hayo.mod_support.jei;

import hayo.common.HayoGuiSprites;
import hayo.energy.EnergyTexts;
import hayo.processing_machine.classic.ElectricFurnaceBlockEntity;
import hayo.processing_machine.matter_generator.MatterGeneratingRecipe;
import hayo.processing_machine.matter_generator.MatterGeneratorBlockEntity;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static hayo.mod_support.jei.HayoJeiWidgets.UPGRADE_SLOT_DRAWABLE;

public class MatterGeneratingJeiCategory implements IRecipeCategory<RecipeHolder<MatterGeneratingRecipe>> {
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
        return 100;
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<MatterGeneratingRecipe> holder, IFocusGroup focuses) {
        if (holder.value().requiredUpgrade().value() != Items.AIR) {
            builder.addSlot(RecipeIngredientRole.INPUT, 1, 5)
                    .setBackground(UPGRADE_SLOT_DRAWABLE, -1, -1)
                    .addRichTooltipCallback((_, tooltip) -> tooltip.add(Component.translatable("hayo.required_upgrade").withStyle(ChatFormatting.YELLOW)))
                    .add(holder.value().requiredUpgrade().value());
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 79, 5) //
                .setOutputSlotBackground() //
                .add(holder.value().result().create());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<MatterGeneratingRecipe> holder, IFocusGroup focuses) {
        var recipe = holder.value();

        int energyCost = recipe.energyCost();
        int useRate = MatterGeneratorBlockEntity.ENERGY_USE_RATE;
        float duration = Mth.positiveCeilDiv(energyCost, useRate) / 20F;

        builder.addDrawableWidget(HayoJeiWidgets.zapWidget(energyCost, useRate))
                .setPosition(23, 5)
                .setTooltip(List.of(
                        EnergyTexts.amount(energyCost),
                        Component.translatable("hayo.machine.recipe_duration", duration, useRate).withStyle(ChatFormatting.GRAY)
                ));

        builder.addText(EnergyTexts.amount(energyCost), Integer.MAX_VALUE, Integer.MAX_VALUE).setColor(0xFF404040).setPosition(1, 28);
    }

    @Override
    public void draw(RecipeHolder<MatterGeneratingRecipe> holder, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        int energyCost = holder.value().energyCost();
        float fill = (System.currentTimeMillis() / 50 * ElectricFurnaceBlockEntity.ENERGY_USE_RATE) % energyCost;
        HayoGuiSprites.blitRecipeArrow(graphics, 44, 4, HayoGuiSprites.DEFAULT_ARROW, HayoGuiSprites.DEFAULT_ARROW_OVERLAY, (int) fill, energyCost);
    }
}
