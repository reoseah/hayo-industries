package hayo.mod_support.jei.widgets;

import hayo.common.HayoSprites;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class SpriteJeiDrawable implements IDrawable {
    public static final IDrawable SLOT = new SpriteJeiDrawable(HayoSprites.SLOT, 18, 18);
    public static final IDrawable UPGRADE_SLOT = new SpriteJeiDrawable(HayoSprites.UPGRADE_SLOT, 18, 18);

    private final Identifier spriteId;
    private final int width;
    private final int height;

    public SpriteJeiDrawable(Identifier spriteId, int width, int height) {
        this.spriteId = spriteId;
        this.width = width;
        this.height = height;
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
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.spriteId, xOffset, yOffset, this.width, this.height);
    }
}
