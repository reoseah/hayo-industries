package hayo.mod_support.jei;

import hayo.energy.EnergyTexts;
import hayo.features.processing_machine.classic.ClassicMachineRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import org.jspecify.annotations.Nullable;

import java.util.List;

public abstract class SingleItemJeiCategory<T extends RecipeHolder<? extends SingleItemRecipe>> implements IRecipeCategory<T> {
    private final IDrawable icon;
    public final @Nullable ItemStack requiredUpgrade;

    protected SingleItemJeiCategory(IDrawable icon, @Nullable ItemStack requiredUpgrade) {
        this.icon = icon;
        this.requiredUpgrade = requiredUpgrade;
    }

    protected final boolean canHaveCatalyst() {
        return this.requiredUpgrade != null;
    }

    protected final ItemStack getCatalyst(T holder) {
        return this.requiredUpgrade;
    }

    protected abstract boolean canHaveSecondaryResult();

    protected abstract int getEnergyCost(T holder);

    protected abstract int getEnergyUseRate(T holder);

    protected abstract Identifier getArrowSprite();

    protected abstract Identifier getArrowOverlaySprite();

    @Override
    public final IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public int getWidth() {
        int width = 82;
        if (this.canHaveCatalyst()) {
            width += 20;
        }
        if (this.canHaveSecondaryResult()) {
            width += 20;
        }
        return width;
    }

    @Override
    public int getHeight() {
        return 35;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, T holder, IFocusGroup focuses) {
        var recipe = holder.value();
        if (this.canHaveCatalyst() && !this.getCatalyst(holder).isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                    .setBackground(HayoJeiWidgets.UPGRADE_SLOT_DRAWABLE, -1, -1)
                    .addRichTooltipCallback((_, tooltip) -> tooltip.add(Component.translatable("hayo.required_upgrade").withStyle(ChatFormatting.YELLOW)))
                    .add(this.getCatalyst(holder));
        }

        int left = this.canHaveCatalyst() ? 20 : 0;

        var inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, left + 1, 1).setStandardSlotBackground();
        if (recipe instanceof ClassicMachineRecipe machineRecipe && machineRecipe.inputCount != 1) {
            inputSlot.addItemStacks(recipe.input().items().map(item -> new ItemStack(item, machineRecipe.inputCount)).toList());
        } else {
            inputSlot.add(recipe.input());
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, left + 61, 5)
                .setOutputSlotBackground()
                .add(recipe.result().create());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, T holder, IFocusGroup focuses) {
        int left = this.canHaveCatalyst() ? 20 : 0;

        int energyCost = this.getEnergyCost(holder);
        int useRate = this.getEnergyUseRate(holder);
        float duration = Mth.positiveCeilDiv(energyCost, useRate) / 20F;

        builder.addDrawableWidget(HayoJeiWidgets.zapWidget(energyCost, useRate))
                .setPosition(left + 1, 20)
                .setTooltip(List.of(
                        EnergyTexts.amount(energyCost),
                        Component.translatable("hayo.machine.recipe_duration", duration, useRate).withStyle(ChatFormatting.GRAY)
                ));
        builder.addText(EnergyTexts.amount(energyCost), Integer.MAX_VALUE, Integer.MAX_VALUE)
                .setColor(0xFF404040)
                .setPosition(left + 19, 24);

        builder.addDrawableWidget(HayoJeiWidgets.recipeArrow(energyCost, useRate, this.getArrowSprite(), this.getArrowOverlaySprite()))
                .setPosition(left + 24, 4);

        if (holder.value() instanceof ClassicMachineRecipe machineRecipe && machineRecipe.extraResultChance > 0) {
            builder.addDrawableWidget(HayoJeiWidgets.itemWithChance(machineRecipe.result().create(), machineRecipe.extraResultChance))
                    .setPosition(left + 84, 0)
                    .setTooltip(Component.translatable("hayo.extra_chance_tooltip", String.format("%.0f", 100 * machineRecipe.extraResultChance)).withStyle(ChatFormatting.YELLOW));
        }
    }
}
