package io.github.reoseah.hayo.feature.universal_screen;

import io.github.reoseah.hayo.Hayo;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.function.IntSupplier;

public record MachineEnergyBar(int x, int y, IntSupplier amount,
                               IntSupplier capacity) implements UniversalContainerMenu.GuiElement {
    public static final Identifier ENERGY_SMALL = Hayo.modId("energy_small");
    public static final Identifier ENERGY_SMALL_OVERLAY = Hayo.modId("energy_small_overlay");

    @Override
    public void extract(GuiGraphicsExtractor graphics, Font font, int left, int top, int mouseX, int mouseY, float partialTick) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ENERGY_SMALL, left + this.x, top + this.y, 14, 14);

        int amount = this.amount.getAsInt();
        int capacity = this.capacity.getAsInt();

        if (amount > 0 && capacity > 0) {
            var filled = Mth.clamp(1 + (14 - 1) * amount / capacity, 1, 14);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ENERGY_SMALL_OVERLAY, 14, 14, 0, 14 - filled, left + this.x, top + this.y + 14 - filled, 14, filled);
        }
    }
}
