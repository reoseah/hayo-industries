package io.github.reoseah.hayo.mod_support.jei;

import io.github.reoseah.hayo.Hayo;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

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

    public static void extract(GuiGraphicsExtractor graphics, int x, int y, RecipeArrow arrowType, int currentProgress, int totalProgress) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, arrowType.background, x, y, 24, 16);

        if (currentProgress > 0 && totalProgress > 0) {
            int length = Mth.clamp(1 + 24 * currentProgress / totalProgress, 0, 24);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, arrowType.overlay, 24, 16, 0, 0, x, y, length, 16);
        }
    }
}
