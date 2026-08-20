package hayo.old_menus;

import hayo.Hayo;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.function.IntSupplier;

public record RecipeProgressBar(Identifier sprite, Identifier overlaySprite, int x, int y, IntSupplier progress,
                                IntSupplier maxProgress) implements UniversalContainerMenu.GuiElement {
    public static final Identifier DEFAULT = Hayo.modId("recipe_arrows/default");
    public static final Identifier DEFAULT_OVERLAY = Hayo.modId("recipe_arrows/default_overlay");
    public static final Identifier MACERATOR = Hayo.modId("recipe_arrows/macerating");
    public static final Identifier MACERATOR_OVERLAY = Hayo.modId("recipe_arrows/macerating_overlay");
    public static final Identifier COMPRESSOR = Hayo.modId("recipe_arrows/compressing");
    public static final Identifier COMPRESSOR_OVERLAY = Hayo.modId("recipe_arrows/compressing_overlay");
    public static final Identifier EXTRACTOR = Hayo.modId("recipe_arrows/extracting");
    public static final Identifier EXTRACTOR_OVERLAY = Hayo.modId("recipe_arrows/extracting_overlay");

    public static RecipeProgressBar defaultArrow(int x, int y, IntSupplier progress, IntSupplier maxProgress) {
        return new RecipeProgressBar(DEFAULT, DEFAULT_OVERLAY, x, y, progress, maxProgress);
    }

    public static RecipeProgressBar macerator(int x, int y, IntSupplier progress, IntSupplier maxProgress) {
        return new RecipeProgressBar(MACERATOR, MACERATOR_OVERLAY, x, y, progress, maxProgress);
    }

    public static RecipeProgressBar compressor(int x, int y, IntSupplier progress, IntSupplier maxProgress) {
        return new RecipeProgressBar(COMPRESSOR, COMPRESSOR_OVERLAY, x, y, progress, maxProgress);
    }

    public static RecipeProgressBar extractor(int x, int y, IntSupplier progress, IntSupplier maxProgress) {
        return new RecipeProgressBar(EXTRACTOR, EXTRACTOR_OVERLAY, x, y, progress, maxProgress);
    }

    @Override
    public void extract(GuiGraphicsExtractor graphics, Font font, int left, int top, int mouseX, int mouseY, float partialTick) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprite, left + this.x, top + this.y, 24, 16);

        int progress = this.progress.getAsInt();
        int maxProgress = this.maxProgress.getAsInt();

        if (progress > 0 && maxProgress > 0) {
            int length = Mth.clamp(1 + 24 * progress / maxProgress, 0, 24);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.overlaySprite, 24, 16, 0, 0, left + this.x, top + this.y, length, 16);
        }
    }
}
