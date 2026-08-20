package hayo.old_menus;

import hayo.Hayo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public record SpriteElement(Identifier sprite, int x, int y, int width,
                            int height) implements UniversalContainerMenu.GuiElement {
    public static final Identifier SLOT = Hayo.modId("slots/default");
    public static final Identifier OUTPUT_SLOT = Hayo.modId("slots/output");
    public static final Identifier UPGRADE_SLOT = Hayo.modId("slots/upgrade");

    public static SpriteElement slot(int x, int y) {
        return new SpriteElement(SLOT, x, y, 18, 18);
    }

    public static SpriteElement outputSlot(int x, int y) {
        return new SpriteElement(OUTPUT_SLOT, x, y, 24, 24);
    }

    public static SpriteElement upgradeSlot(int x, int y) {
        return new SpriteElement(UPGRADE_SLOT, x, y, 18, 18);
    }

    public static final Identifier SMALL_ARROW_RIGHT = Hayo.modId("small_arrow_right");
    public static final Identifier SMALL_ARROW_LEFT = Hayo.modId("small_arrow_left");

    public static SpriteElement smallArrowLeft(int x, int y) {
        return new SpriteElement(SMALL_ARROW_LEFT, x, y, 9, 18);
    }

    public static SpriteElement smallArrowRight(int x, int y) {
        return new SpriteElement(SMALL_ARROW_RIGHT, x, y, 9, 18);
    }

    public static final Identifier SLOT_CONNECTION_9_WIDE = Hayo.modId("slot_connection_9_wide");

    public static SpriteElement slotConnection9Wide(int x, int y) {
        return new SpriteElement(SLOT_CONNECTION_9_WIDE, x, y, 11, 2);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void extract(GuiGraphicsExtractor graphics, Font font, int left, int top, int mouseX, int mouseY, float partialTick) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprite, left + this.x, top + this.y, this.width, this.height);
    }
}
