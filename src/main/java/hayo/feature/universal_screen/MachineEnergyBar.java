package hayo.feature.universal_screen;

import hayo.energy.client.EnergySprites;
import hayo.energy.EnergyTexts;
import hayo.menu.UniversalContainerMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.IntSupplier;

/// Small zap such as in processing machines, replacing the flame in vanilla furnaces.
public record MachineEnergyBar(int x, int y, IntSupplier amount,
                               IntSupplier capacity) implements UniversalContainerMenu.GuiElement {
    @Override
    public void extract(GuiGraphicsExtractor graphics, Font font, int left, int top, int mouseX, int mouseY, float partialTick) {
        EnergySprites.extractZap(graphics, left + this.x, top + this.y, this.amount.getAsInt(), this.capacity.getAsInt());
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
