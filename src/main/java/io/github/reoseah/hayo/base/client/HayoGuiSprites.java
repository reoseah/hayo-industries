package io.github.reoseah.hayo.base.client;

import io.github.reoseah.hayo.Hayo;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class HayoGuiSprites {
    public static final Identifier SMALL_ARROW_RIGHT = Hayo.modId("small_arrow_right");
    public static final Identifier SMALL_ARROW_LEFT = Hayo.modId("small_arrow_left");

    public static void drawSmallArrowRight(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SMALL_ARROW_RIGHT, x, y, 9, 18);
    }

    public static void drawSmallArrowLeft(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SMALL_ARROW_LEFT, x, y, 9, 18);
    }

    public static final Identifier SLOT = Hayo.modId("slots/default");

    public static void drawSlot(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT, x, y, 18, 18);
    }

    public static final Identifier UPGRADE_SLOT = Hayo.modId("slots/upgrade");

    public static void drawUpgradeSlot(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, UPGRADE_SLOT, x, y, 18, 18);
    }

    public static final Identifier OUTPUT_SLOT = Hayo.modId("slots/output");

    public static void drawOutputSlot(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, OUTPUT_SLOT, x, y, 24, 24);
    }

    public static void drawRecipeArrow(GuiGraphicsExtractor graphics, int x, int y, RecipeArrow arrowType, int currentProgress, int totalProgress) {
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
}
