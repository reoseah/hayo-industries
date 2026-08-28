package hayo.mod_support.jei;

import hayo.common.HayoGuiSprites;
import hayo.energy.EnergyTexts;
import hayo.energy.client.EnergyGuiSprites;
import hayo.processing_machine.classic.ClassicMachineRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.gui.elements.DrawableSprite;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleItemRecipe;

public abstract class SingleItemRecipeCategory<T extends RecipeHolder<? extends SingleItemRecipe>> implements IRecipeCategory<T> {
    public static final IDrawable UPGRADE_SLOT_DRAWABLE = new DrawableSprite(Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.GUI), HayoGuiSprites.UPGRADE_SLOT, 18, 18);

    protected abstract boolean hasCatalyst();

    protected ItemStack getCatalyst(T holder) {
        return ItemStack.EMPTY;
    }

    protected abstract boolean hasSecondaryResult();

    protected abstract int getEnergyCost(T holder);

    protected abstract int getEnergyUseRate(T holder);

    protected abstract Identifier getArrowSprite();

    protected abstract Identifier getArrowOverlaySprite();

    @Override
    public int getWidth() {
        int width = 82;
        if (this.hasCatalyst()) {
            width += 20;
        }
        if (this.hasSecondaryResult()) {
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
        if (this.hasCatalyst() && !this.getCatalyst(holder).isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                    .setBackground(UPGRADE_SLOT_DRAWABLE, -1, -1)
                    .addRichTooltipCallback((_, tooltip) -> tooltip.add(Component.translatable("hayo.required_upgrade").withStyle(ChatFormatting.YELLOW)))
                    .add(this.getCatalyst(holder));
        }

        int x = this.hasCatalyst() ? 20 : 0;
        if (holder.value() instanceof ClassicMachineRecipe machineRecipe && machineRecipe.inputCount != 1) {
            builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                    .setStandardSlotBackground()
                    .addItemStacks(holder.value().input().items().map(item -> new ItemStack(item, machineRecipe.inputCount)).toList());
        } else {
            builder.addSlot(RecipeIngredientRole.INPUT, x + 1, 1)
                    .setStandardSlotBackground()
                    .add(holder.value().input());
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, x + 61, 5)
                .setOutputSlotBackground()
                .add(holder.value().result().create());
        if (holder.value() instanceof ClassicMachineRecipe machineRecipe && machineRecipe.extraResultChance > 0) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 85, 1)
                    .setStandardSlotBackground()
                    .addRichTooltipCallback((_, tooltip) -> tooltip.add(Component.translatable("hayo.chance.words", machineRecipe.extraResultChance * 100).withStyle(ChatFormatting.YELLOW)))
                    .add(holder.value().result().create().copyWithCount(1));
        }
    }

    @Override
    public void draw(T holder, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        int x = this.hasCatalyst() ? 20 : 0;

        EnergyGuiSprites.blitZap(graphics, x + 1, 20, 10, 14);

        int energyCost = this.getEnergyCost(holder);
        int energyUseRate = this.getEnergyUseRate(holder);
        int fill = (int) ((int) System.currentTimeMillis() / 50 * energyUseRate) % energyCost;
        HayoGuiSprites.blitRecipeArrow(graphics, x + 24, 4, this.getArrowSprite(), this.getArrowOverlaySprite(), (int) fill, energyCost);

        var font = Minecraft.getInstance().font;
        graphics.text(font, EnergyTexts.amount(energyCost), x + 19, 24, 0xFF404040, false);
        if (holder.value() instanceof ClassicMachineRecipe machineRecipe && machineRecipe.extraResultChance > 0) {
            var extraChance = Component.translatable("hayo.chance.percentage", String.format("%.0f", 100 * machineRecipe.extraResultChance));
            graphics.text(font, extraChance, x + 85, 24, 0xFF404040, false);
        }
    }
}
