package io.github.reoseah.hayo.feature.electric_blocks;

import io.github.reoseah.hayo.Hayo;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class EnergyGuiSprites {
    public static final Identifier ENERGY_SMALL = Hayo.modId("energy_small");
    public static final Identifier ENERGY_SMALL_OVERLAY = Hayo.modId("energy_small_overlay");

    /// Small zap such as in processing machines, replacing the flame in vanilla furnaces.
    public static void energySmall(GuiGraphicsExtractor graphics, int x, int y, int energy, int capacity) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ENERGY_SMALL, x, y, 14, 14);

        if (energy > 0 && capacity > 0) {
            var height = Mth.clamp(1 + (14 - 1) * energy / capacity, 1, 14);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ENERGY_SMALL_OVERLAY, 14, 14, 0, 14 - height, x, y + 14 - height, 14, height);
        }
    }
}
