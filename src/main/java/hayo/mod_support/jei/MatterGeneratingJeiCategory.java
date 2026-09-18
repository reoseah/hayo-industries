package hayo.mod_support.jei;

import hayo.common.HayoGuiSprites;
import hayo.energy.EnergyTexts;
import hayo.energy.client.EnergyGuiSprites;
import hayo.processing_machine.classic.ElectricFurnaceBlockEntity;
import hayo.processing_machine.matter_generator.MatterGeneratingRecipe;
import hayo.processing_machine.matter_generator.MatterGeneratorBlockEntity;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static hayo.mod_support.jei.SingleItemRecipeCategory.UPGRADE_SLOT_DRAWABLE;

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
    public void draw(RecipeHolder<MatterGeneratingRecipe> holder, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        EnergyGuiSprites.blitZap(graphics, 23, 5, 10, 14);

        int energyCost = holder.value().energyCost();
        float fill = (System.currentTimeMillis() / 50 * ElectricFurnaceBlockEntity.ENERGY_USE_RATE) % energyCost;
        HayoGuiSprites.blitRecipeArrow(graphics, 44, 4, HayoGuiSprites.DEFAULT_ARROW, HayoGuiSprites.DEFAULT_ARROW_OVERLAY, (int) fill, energyCost);

        graphics.text(Minecraft.getInstance().font, EnergyTexts.amount(energyCost), 1, 28, 0xFF404040, false);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<MatterGeneratingRecipe> holder, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (mouseX >= 23 && mouseX <= 23 + 14 && mouseY >= 5 && mouseY <= 5 + 14 //
                || mouseX > 42 && mouseX <= 42 + 24 && mouseY > 4 && mouseY <= 4 + 16) {
            var recipe = holder.value();
            float duration = Mth.positiveCeilDiv(recipe.energyCost(), MatterGeneratorBlockEntity.ENERGY_USE_RATE) / 20F;

            tooltip.addAll(List.of( //
                    Component.translatable("hayo.duration.seconds", duration), //
                    EnergyTexts.amountAndAmountPerTick(recipe.energyCost(), MatterGeneratorBlockEntity.ENERGY_USE_RATE).withStyle(ChatFormatting.GRAY)));
        }
    }
}
