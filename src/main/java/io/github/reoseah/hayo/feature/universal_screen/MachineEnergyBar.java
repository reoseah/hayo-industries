package io.github.reoseah.hayo.feature.universal_screen;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.electric_blocks.EnergyTexts;
import io.github.reoseah.hayo.menu.UniversalContainerMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.IntSupplier;

/// Small zap such as in processing machines, replacing the flame in vanilla furnaces.
public record MachineEnergyBar(int x, int y, IntSupplier amount,
                               IntSupplier capacity) implements UniversalContainerMenu.GuiElement {
    public static final Identifier ENERGY_SMALL = Hayo.modId("energy_small");
    public static final Identifier ENERGY_SMALL_OVERLAY = Hayo.modId("energy_small_overlay");

    public static void extract(GuiGraphicsExtractor graphics, int x, int y, int energy, int capacity) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ENERGY_SMALL, x, y, 14, 14);

        if (energy > 0 && capacity > 0) {
            var height = Mth.clamp(1 + (14 - 1) * energy / capacity, 1, 14);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ENERGY_SMALL_OVERLAY, 14, 14, 0, 14 - height, x, y + 14 - height, 14, height);
        }
    }

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

    @Override
    @Environment(EnvType.CLIENT)
    public void extractTooltip(GuiGraphicsExtractor graphics, Font font, int left, int top, int mouseX, int mouseY) {
        if (UniversalContainerMenu.GuiElement.isHovering(left, top, this.x, this.y, 14, 14, mouseX, mouseY)) {
            var tooltip = new ArrayList<Component>();
            int amount = this.amount.getAsInt();
            int capacity = this.capacity.getAsInt();

            tooltip.add(EnergyTexts.amountAndPercentage(amount, capacity));
            tooltip.add(EnergyTexts.maxAmount(capacity).withStyle(ChatFormatting.GRAY));

            graphics.setTooltipForNextFrame(font, tooltip, Optional.empty(), mouseX, mouseY);
        }
    }
}
