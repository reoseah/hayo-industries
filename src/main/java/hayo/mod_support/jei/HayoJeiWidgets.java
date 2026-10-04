package hayo.mod_support.jei;

import hayo.common.HayoGuiSprites;
import hayo.energy.client.EnergyGuiSprites;
import hayo.features.fluid_stack.FluidGuiRendering;
import hayo.features.fluid_stack.FluidIngredientAmount;
import hayo.features.fluid_stack.FluidStack;
import hayo.features.processing_machine.solid_fluid_reactor.ItemTemplateWithChance;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.common.gui.elements.DrawableAnimated;
import mezz.jei.common.gui.elements.DrawableCombined;
import mezz.jei.common.gui.elements.DrawableSprite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class HayoJeiWidgets {
    public static final IDrawableStatic SLOT_DRAWABLE = new DrawableSprite(Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.GUI), HayoGuiSprites.SLOT, 18, 18);
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

    public static IDrawable recipeArrow(int cost, int progressPerTick, Identifier arrowId, Identifier arrowOverlayId) {
        return recipeArrow(Mth.positiveCeilDiv(cost, progressPerTick), arrowId, arrowOverlayId);
    }

    public static IDrawable recipeArrow(int ticksPerCycle, Identifier arrowId, Identifier arrowOverlayId) {
        return new DrawableCombined(
                sprite(arrowId, 24, 16),
                new DrawableAnimated(sprite(arrowOverlayId, 24, 16), ticksPerCycle, IDrawableAnimated.StartDirection.LEFT, false)
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

    public static IDrawable machineWithUpgrade(IGuiHelper guiHelper, ItemLike machine, ItemLike upgrade) {
        var machineDrawable = guiHelper.createDrawableItemLike(machine);
        var upgradeDrawable = guiHelper.createDrawableItemLike(upgrade);

        return new IDrawable() {
            @Override
            public int getWidth() {
                return 16;
            }

            @Override
            public int getHeight() {
                return 16;
            }

            @Override
            public void draw(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
                machineDrawable.draw(graphics, xOffset, yOffset);

                var poseStack = graphics.pose();
                poseStack.pushMatrix();
                {
                    poseStack.translate(8 + xOffset, 8 + yOffset);
                    poseStack.scale(0.5f, 0.5f);
                    upgradeDrawable.draw(graphics);
                }
                poseStack.popMatrix();
            }
        };
    }

    public static IDrawable itemWithChance(ItemTemplateWithChance templateWithChance) {
        return itemWithChance(templateWithChance.template().create(), templateWithChance.chance());
    }

    public static IDrawable itemWithChance(ItemStack stack, float chance) {
        return new IDrawable() {
            @Override
            public int getWidth() {
                return 18;
            }

            @Override
            public int getHeight() {
                return 18;
            }

            @Override
            public void draw(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
                HayoGuiSprites.blitSlot(graphics, xOffset, yOffset);

                graphics.fakeItem(stack, xOffset + 1, yOffset + 1);

                var font = Minecraft.getInstance().font;
                var subscriptChance = Component.translatable("hayo.subscript_chance", toSubscriptDigits(chance));
                int chanceWidth = font.width(subscriptChance);
                graphics.text(font, subscriptChance, xOffset + 18 - chanceWidth, yOffset + 18 - font.lineHeight, 0xFFFFFFFF, true);
            }
        };
    }

    public static String toSubscriptDigits(float chance) {
        return String.format("%.0f", 100 * chance)
                .chars()
                .map(ch -> {
                    char[] subscriptDigits = {'₀', '₁', '₂', '₃', '₄', '₅', '₆', '₇', '₈', '₉'};
                    if (ch >= '0' && ch <= '9') {
                        return subscriptDigits[ch - '0'];
                    }
                    return ch;
                })
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
