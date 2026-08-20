package hayo.common;

import hayo.Hayo;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class HayoGuiSprites {
    public static final Identifier BACKGROUND = Hayo.modId("background");

    public static void extractBackground(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, width, height);
    }

    public static final Identifier FUEL = Hayo.modId("fuel");
    public static final Identifier FUEL_OVERLAY = Hayo.modId("fuel_overlay");

    public static void extractFuel(GuiGraphicsExtractor graphics, int x, int y, int fuelLeft, int fuelTotal) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FUEL, x, y, 14, 14);

        if (fuelLeft > 0 && fuelTotal > 0) {
            var level = Math.clamp(1 + (14 - 1) * fuelLeft / fuelTotal, 1, 14);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FUEL_OVERLAY, 14, 14, 0, 14 - level, x, y + 14 - level, 14, level);
        }
    }

    public static final Identifier SLOT = Hayo.modId("slots/default");

    public static void extractSlot(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT, x, y, 18, 18);
    }

    public static void extractStandardPlayerSlots(GuiGraphicsExtractor graphics, int left, int top) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT, left, top, 18 * 9, 18 * 3);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT, left, top + 18 * 3 + 4, 18 * 9, 18);
    }

    public static final Identifier SMALL_ARROW_RIGHT = Hayo.modId("small_arrow_right");
    public static final Identifier SMALL_ARROW_LEFT = Hayo.modId("small_arrow_left");

    public static void extractSmallArrowRight(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SMALL_ARROW_RIGHT, x, y, 9, 18);
    }

    public static void extractSmallArrowLeft(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SMALL_ARROW_LEFT, x, y, 9, 18);
    }
}
