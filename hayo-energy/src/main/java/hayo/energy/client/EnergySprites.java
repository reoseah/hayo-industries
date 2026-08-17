package hayo.energy.client;

import hayo.energy.impl.HayoEnergy;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class EnergySprites {
    public static final Identifier ZAP = HayoEnergy.modId("energy/zap");
    public static final Identifier ZAP_OVERLAY = HayoEnergy.modId("energy/zap_overlay");

    public static final Identifier VERTICAL_BAR = HayoEnergy.modId("energy/vertical_bar");
    public static final Identifier VERTICAL_BAR_OVERLAY = HayoEnergy.modId("energy/vertical_bar_overlay");

    @Environment(EnvType.CLIENT)
    public static void extractZap(GuiGraphicsExtractor graphics, int x, int y, int energy, int capacity) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ZAP, x, y, 14, 14);

        if (energy > 0 && capacity > 0) {
            var level = Mth.clamp(1 + (14 - 1) * energy / capacity, 1, 14);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ZAP_OVERLAY, 14, 14, 0, 14 - level, x, y + 14 - level, 14, level);
        }
    }

    @Environment(EnvType.CLIENT)
    public static void extractVerticalBar(GuiGraphicsExtractor graphics, int x, int y, int energy, int capacity) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, VERTICAL_BAR, x, y, 18, 56);

        if (energy > 0 && capacity > 0) {
            var level = Mth.clamp(1 + (48 - 1) * energy / capacity, 1, 48);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, VERTICAL_BAR_OVERLAY, 10, 48, 0, 48 - level, x + 4, y + 52 - level, 10, level);
        }
    }
}
