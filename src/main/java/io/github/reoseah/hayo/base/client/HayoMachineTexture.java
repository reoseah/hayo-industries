package io.github.reoseah.hayo.base.client;

import io.github.reoseah.hayo.Hayo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class HayoMachineTexture {
    public static final Identifier LOCATION = Hayo.modId("textures/gui/container/machine.png");

    public static void blit(GuiGraphics graphics, int x, int y, float u, float v, int width, int height) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, LOCATION, x, y, u, v, width, height, 256, 256);
    }
}
