package hayo.mod_support.jei.widgets;

import hayo.features.fluid_stack.FluidGuiRendering;
import hayo.features.fluid_stack.FluidIngredientAmount;
import hayo.features.fluid_stack.FluidStack;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public abstract class FluidTankJeiDrawable implements IDrawable {
    public static IDrawable ofStack(FluidStack fluidStack, int maxAmount) {
        return new FluidTankJeiDrawable() {
            @Override
            public void draw(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
                FluidGuiRendering.extractFluidTank(graphics, fluidStack, maxAmount, xOffset, yOffset);
            }
        };
    }

    public static IDrawable ofIngredient(FluidIngredientAmount ingredient, int maxAmount) {
        return new FluidTankJeiDrawable() {
            @Override
            public void draw(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
                FluidGuiRendering.extractFluidTank(graphics, ingredient, maxAmount, xOffset, yOffset);
            }
        };
    }

    @Override
    public int getWidth() {
        return 18;
    }

    @Override
    public int getHeight() {
        return 56;
    }
}
