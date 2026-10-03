package hayo.mod_support.jei;

import hayo.common.HayoGuiSprites;
import hayo.energy.client.EnergyGuiSprites;
import hayo.features.fluid_stack.FluidGuiRendering;
import hayo.features.fluid_stack.FluidIngredientAmount;
import hayo.features.fluid_stack.FluidStack;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.common.gui.elements.DrawableAnimated;
import mezz.jei.common.gui.elements.DrawableCombined;
import mezz.jei.common.gui.elements.DrawableSprite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class HayoJeiWidgets {
    public static final IDrawableStatic UPGRADE_SLOT_DRAWABLE = new DrawableSprite(Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.GUI), HayoGuiSprites.UPGRADE_SLOT, 18, 18);

    public static IDrawableStatic sprite(Identifier id, int width, int height) {
        return new DrawableSprite(Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.GUI), id, width, height);
    }

    public static IDrawable zapWidget(int cost, int progressPerTick) {
        return zapWidget(Mth.positiveCeilDiv(cost, progressPerTick));
    }

    public static IDrawable zapWidget(int ticksPerCycle) {
        return new DrawableCombined(
                sprite(EnergyGuiSprites.ZAP, 14, 14),
                new DrawableAnimated(sprite(EnergyGuiSprites.ZAP_OVERLAY, 14, 14), ticksPerCycle, IDrawableAnimated.StartDirection.TOP, true)
        );
    }

    public static IDrawable fluidTank(FluidStack fluidStack, int maxAmount) {
        return new IDrawable() {
            @Override
            public int getWidth() {
                return 18;
            }

            @Override
            public int getHeight() {
                return 56;
            }

            @Override
            public void draw(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
                FluidGuiRendering.extractFluidTank(graphics, fluidStack, maxAmount, xOffset, yOffset);
            }
        };
    }

    public static IDrawable fluidTank(FluidIngredientAmount ingredient, int maxAmount) {
        return new IDrawable() {
            @Override
            public int getWidth() {
                return 18;
            }

            @Override
            public int getHeight() {
                return 56;
            }

            @Override
            public void draw(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
                FluidGuiRendering.extractFluidTank(graphics, ingredient, maxAmount, xOffset, yOffset);
            }
        };
    }
}
