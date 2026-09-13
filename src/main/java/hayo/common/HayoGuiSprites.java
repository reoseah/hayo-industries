package hayo.common;

import hayo.Hayo;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class HayoGuiSprites {
    public static final Identifier BACKGROUND = Hayo.modId("background");

    public static void blitBackground(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, width, height);
    }

    public static final Identifier FUEL = Hayo.modId("fuel");
    public static final Identifier FUEL_OVERLAY = Hayo.modId("fuel_overlay");

    public static void blitFuel(GuiGraphicsExtractor graphics, int x, int y, int fuelLeft, int fuelTotal) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FUEL, x, y, 14, 14);

        if (fuelLeft > 0 && fuelTotal > 0) {
            var level = Math.clamp(1 + (14 - 1) * fuelLeft / fuelTotal, 1, 14);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FUEL_OVERLAY, 14, 14, 0, 14 - level, x, y + 14 - level, 14, level);
        }
    }

    public static final Identifier SLOT = Hayo.modId("slots/default");

    public static void blitSlot(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT, x, y, 18, 18);
    }

    public static void blitSlots(GuiGraphicsExtractor graphics, int x, int y, int columns, int rows) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT, x, y, 18 * columns, 18 * rows);
    }

    public static void blitStandardPlayerSlots(GuiGraphicsExtractor graphics, int left, int top) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT, left, top, 18 * 9, 18 * 3);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT, left, top + 18 * 3 + 4, 18 * 9, 18);
    }

    public static final Identifier OUTPUT_SLOT = Hayo.modId("slots/output");

    public static void blitOutputSlot(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, OUTPUT_SLOT, x, y, 24, 24);
    }

    public static final Identifier UPGRADE_SLOT = Hayo.modId("slots/upgrade");

    public static void blitUpgradeSlots(GuiGraphicsExtractor graphics, int left, int top, int count) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, UPGRADE_SLOT, left, top, 18, 18 * count);
    }

    public static final Identifier SLOT_CONNECTION_9_WIDE = Hayo.modId("slot_connection_9_wide");

    public static void blitSlotConnection9Wide(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_CONNECTION_9_WIDE, x, y, 11, 2);
    }

    public static final Identifier DEFAULT_ARROW = Hayo.modId("recipe_arrows/default");
    public static final Identifier DEFAULT_ARROW_OVERLAY = Hayo.modId("recipe_arrows/default_overlay");
    public static final Identifier MACERATING_ARROW = Hayo.modId("recipe_arrows/macerating");
    public static final Identifier MACERATING_ARROW_OVERLAY = Hayo.modId("recipe_arrows/macerating_overlay");
    public static final Identifier COMPRESSING_ARROW = Hayo.modId("recipe_arrows/compressing");
    public static final Identifier COMPRESSING_ARROW_OVERLAY = Hayo.modId("recipe_arrows/compressing_overlay");
    public static final Identifier EXTRACTING_ARROW = Hayo.modId("recipe_arrows/extracting");
    public static final Identifier EXTRACTING_ARROW_OVERLAY = Hayo.modId("recipe_arrows/extracting_overlay");

    public static void blitRecipeArrow(GuiGraphicsExtractor graphics, int x, int y, Identifier arrow, Identifier arrowOverlay, int progress, int maxProgress) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, arrow, x, y, 24, 16);

        if (progress > 0 && maxProgress > 0) {
            int length = Mth.clamp(1 + 24 * progress / maxProgress, 0, 24);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, arrowOverlay, 24, 16, 0, 0, x, y, length, 16);
        }
    }

    public static final Identifier SMALL_ARROW_RIGHT = Hayo.modId("small_arrow_right");
    public static final Identifier SMALL_ARROW_LEFT = Hayo.modId("small_arrow_left");

    public static void blitSmallArrowRight(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SMALL_ARROW_RIGHT, x, y, 9, 18);
    }

    public static void blitSmallArrowLeft(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SMALL_ARROW_LEFT, x, y, 9, 18);
    }

    public static final Identifier FLUID_OVERLAY = Hayo.modId("fluid_overlay");

    public static void blitFluidOverlay(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FLUID_OVERLAY, x, y, 18, 56);
    }

    public static final Identifier DRAINING_ARROW = Hayo.modId("draining_arrow");
    public static final Identifier DRAINING_ARROW_OVERLAY = Hayo.modId("draining_arrow_overlay");

    public static void blitDrainingArrow(GuiGraphicsExtractor graphics, int x, int y, int progress, int maxProgress) {
//        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, DRAINING_ARROW, x, y, 18, 14);

        if (progress > 0 && maxProgress > 0) {
            int length = Mth.clamp(1 + 18 * progress / maxProgress, 0, 18);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, DRAINING_ARROW_OVERLAY, 18, 14, 0, 0, x, y, length, 14);
        }
    }
}
