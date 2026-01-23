package io.github.reoseah.hayo.base.client;

import io.github.reoseah.hayo.Hayo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class HayoGuiSprites {
    public static final Identifier ENERGY_STORAGE = Hayo.modId("energy_storage");
    public static final Identifier ENERGY_STORAGE_OVERLAY = Hayo.modId("energy_storage_overlay");

    public static void drawEnergyStorage(GuiGraphics graphics, int x, int y, int amount, int capacity) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ENERGY_STORAGE, x, y, 18, 56);

        if (amount > 0 && capacity > 0) {
            var height = Mth.clamp(1 + (48 - 1) * amount / capacity, 1, 48);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ENERGY_STORAGE_OVERLAY, 10, 48, 0, 48 - height, x + 4, y + 4 + 48 - height, 10, height);
        }
    }

    public static final Identifier MACHINE_ENERGY = Hayo.modId("machine_energy");
    public static final Identifier MACHINE_ENERGY_OVERLAY = Hayo.modId("machine_energy_overlay");

    public static void drawMachineEnergy(GuiGraphics graphics, int x, int y, int energy, int capacity) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MACHINE_ENERGY, x, y, 14, 14);

        if (energy > 0 && capacity > 0) {
            var height = Mth.clamp(1 + (14 - 1) * energy / capacity, 1, 14);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MACHINE_ENERGY_OVERLAY, 14, 14, 0, 14 - height, x, y + 14 - height, 14, height);
        }
    }

    public static final Identifier FUEL = Hayo.modId("fuel");
    public static final Identifier FUEL_OVERLAY = Hayo.modId("fuel_overlay");

    public static void drawFuel(GuiGraphics graphics, int x, int y, int fuelLeft, int fuelTotal) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FUEL, x, y, 14, 14);

        if (fuelLeft > 0 && fuelTotal > 0) {
            var height = Mth.clamp(1 + (14 - 1) * fuelLeft / fuelTotal, 1, 14);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FUEL_OVERLAY, 14, 14, 0, 14 - height, x, y + 14 - height, 14, height);
        }
    }

    public static final Identifier SMALL_ARROW_RIGHT = Hayo.modId("small_arrow_right");
    public static final Identifier SMALL_ARROW_LEFT = Hayo.modId("small_arrow_left");

    public static void drawSmallArrowRight(GuiGraphics graphics, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SMALL_ARROW_RIGHT, x, y, 9, 18);
    }

    public static void drawSmallArrowLeft(GuiGraphics graphics, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SMALL_ARROW_LEFT, x, y, 9, 18);
    }

    public static final Identifier SLOT = Hayo.modId("slots/default");

    public static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT, x, y, 18, 18);
    }

    public static final Identifier UPGRADE_SLOT = Hayo.modId("slots/upgrade");

    public static void drawUpgradeSlot(GuiGraphics graphics, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, UPGRADE_SLOT, x, y, 18, 18);
    }

    public static final Identifier OUTPUT_SLOT = Hayo.modId("slots/output");

    public static void drawOutputSlot(GuiGraphics graphics, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, OUTPUT_SLOT, x, y, 24, 24);
    }

    public static void drawRecipeArrow(GuiGraphics graphics, int x, int y, RecipeArrow arrowType, int currentProgress, int totalProgress) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, arrowType.background, x, y, 24, 16);

        if (currentProgress > 0 && totalProgress > 0) {
            int length = Mth.clamp(1 + 24 * currentProgress / totalProgress, 0, 24);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, arrowType.overlay, 24, 16, 0, 0, x, y, length, 16);
        }
    }

    public enum RecipeArrow {
        DEFAULT(Hayo.modId("recipe_arrows/default"), Hayo.modId("recipe_arrows/default_overlay")),
        MACERATOR(Hayo.modId("recipe_arrows/macerating"), Hayo.modId("recipe_arrows/macerating_overlay")),
        COMPRESSOR(Hayo.modId("recipe_arrows/compressing"), Hayo.modId("recipe_arrows/compressing_overlay")),
        EXTRACTOR(Hayo.modId("recipe_arrows/extracting"), Hayo.modId("recipe_arrows/extracting_overlay"));

        public final Identifier background, overlay;

        RecipeArrow(Identifier background, Identifier overlay) {
            this.background = background;
            this.overlay = overlay;
        }
    }

    public static final Identifier RECIPE = Hayo.modId("recipe_button/default");
    public static final Identifier RECIPE_SELECTED = Hayo.modId("recipe_button/selected");
    public static final Identifier RECIPE_HIGHLIGHTED = Hayo.modId("recipe_button/highlighted");
    public static final Identifier RECIPE_DISABLED = Hayo.modId("recipe_button/disabled");

    public static final Identifier SCROLLER = Hayo.modId("scroller/default");
    public static final Identifier SCROLLER_DISABLED = Hayo.modId("scroller/disabled");

    public static void drawScroller(GuiGraphics graphics, int x, int y, boolean disabled) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, disabled ? SCROLLER_DISABLED : SCROLLER, x, y, 12, 15);
    }

    public static final Identifier SLOT_CONNECTION_9_WIDE = Hayo.modId("slot_connection_9_wide");

    public static final Identifier BATTERY_SLOT_ICON = Hayo.modId("slot_icons/battery");
}
