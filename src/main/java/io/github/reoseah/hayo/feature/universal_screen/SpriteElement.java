package io.github.reoseah.hayo.feature.universal_screen;

import io.github.reoseah.hayo.Hayo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public record SpriteElement(Identifier sprite, int x, int y, int width,
                            int height) implements UniversalContainerMenu.GuiElement {
    public static final Identifier SMALL_ARROW_RIGHT = Hayo.modId("small_arrow_right");
    public static final Identifier SMALL_ARROW_LEFT = Hayo.modId("small_arrow_left");

    public static SpriteElement smallArrowLeft(int x, int y) {
        return new SpriteElement(SMALL_ARROW_LEFT, x, y, 9, 18);
    }

    public static SpriteElement smallArrowRight(int x, int y) {
        return new SpriteElement(SMALL_ARROW_RIGHT, x, y, 9, 18);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(GuiGraphicsExtractor graphics, Font font, int left, int top, int mouseX, int mouseY, float partialTick) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprite, left + this.x, top + this.y, this.width, this.height);
    }
}
