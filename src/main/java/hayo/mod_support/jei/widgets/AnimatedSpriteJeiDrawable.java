package hayo.mod_support.jei.widgets;

import hayo.energy.client.EnergyGuiSprites;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

public class AnimatedSpriteJeiDrawable implements IDrawable {
    public static IDrawable zapWidget(int cost, int progressPerTick) {
        return zapWidget(Mth.positiveCeilDiv(cost, progressPerTick));
    }

    public static IDrawable zapWidget(int ticksPerCycle) {
        return new AnimatedSpriteJeiDrawable(
                EnergyGuiSprites.ZAP,
                EnergyGuiSprites.ZAP_OVERLAY,
                14,
                14,
                ScreenDirection.UP,
                ticksPerCycle,
                true
        );
    }

    public static IDrawable recipeArrow(int cost, int progressPerTick, Identifier arrowId, Identifier arrowOverlayId) {
        return recipeArrow(Mth.positiveCeilDiv(cost, progressPerTick), arrowId, arrowOverlayId);
    }

    public static IDrawable recipeArrow(int ticksPerCycle, Identifier arrowId, Identifier arrowOverlayId) {
        return new AnimatedSpriteJeiDrawable(
                arrowId,
                arrowOverlayId,
                24,
                16,
                ScreenDirection.LEFT,
                ticksPerCycle,
                false
        );
    }

    private final @Nullable Identifier baseSpriteId;
    private final Identifier overlaySpriteId;
    private final int width;
    private final int height;
    private final ScreenDirection direction;
    private final int ticks;
    private final boolean countDown;

    public AnimatedSpriteJeiDrawable(Identifier baseSpriteId, Identifier overlaySpriteId, int width, int height, ScreenDirection direction, int ticks, boolean countDown) {
        this.baseSpriteId = baseSpriteId;
        this.overlaySpriteId = overlaySpriteId;
        this.width = width;
        this.height = height;
        this.direction = direction;
        this.ticks = ticks;
        this.countDown = countDown;
    }


    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset) {
        if (this.baseSpriteId != null) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.baseSpriteId, xOffset, yOffset, this.width, this.height);
        }

        int maxLength = this.direction.getAxis() == ScreenAxis.VERTICAL ? this.height : this.width;
        int length = (int) Math.floorDiv(System.currentTimeMillis() % (this.ticks * 50) * (maxLength + 1), this.ticks * 50);
        if (this.countDown) {
            length = maxLength - length;
        }

        int offsetLeft = 0, offsetRight = 0, offsetUp = 0, offsetDown = 0;
        switch (this.direction) {
            case UP -> offsetDown = length;
            case DOWN -> offsetUp = length;
            case LEFT -> offsetRight = length;
            case RIGHT -> offsetLeft = length;
        }

        int uWidth = this.width - (offsetRight + offsetLeft);
        int vHeight = this.height - (offsetDown + offsetUp);

        guiGraphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                this.overlaySpriteId,
                this.width,
                this.height,
                offsetLeft,
                offsetUp,
                xOffset + offsetLeft,
                yOffset + offsetUp,
                uWidth,
                vHeight
        );
    }
}
